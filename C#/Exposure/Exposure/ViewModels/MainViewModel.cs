using System.Collections.ObjectModel;
using Windows.Storage;
using Windows.System;
using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using CommunityToolkit.WinUI;
using Exposure.Contracts.Services;
using Exposure.Helpers;
using Exposure.Logging;
using Exposure.Models;
using Microsoft.UI.Xaml;
using DispatcherQueue = Microsoft.UI.Dispatching.DispatcherQueue;

namespace Exposure.ViewModels;

public partial class MainViewModel : ObservableRecipient, IDisposable
{
    private readonly DispatcherQueue _dispatcherQueue;
    private readonly DispatcherTimer _dispatcherTimer;
    private readonly ILocalSettingsService _localSettingsService;
    private readonly IPictureService _pictureService;
    private readonly IVisionService _visionService;
    [ObservableProperty] private int _exposureTime = 100;
    [ObservableProperty] private string? _image;
    [ObservableProperty] private int _offsetX;
    [ObservableProperty] private int _offsetY;
    [ObservableProperty] private string _status = "\uF384";
    [ObservableProperty] private string _temperature = "OFF";
    [ObservableProperty] private string _calibrate = "Calibrate".GetAppLocalized();
    [ObservableProperty] private string _shooting = "Shooting".GetAppLocalized();
    public ObservableCollection<Picture> Pictures { get; } = new();

    public MainViewModel(IVisionService visionService, IPictureService pictureService,
        ILocalSettingsService localSettingsService)
    {
        _visionService = visionService;
        _pictureService = pictureService;
        _localSettingsService = localSettingsService;

        _dispatcherTimer = new DispatcherTimer();
        _dispatcherQueue = DispatcherQueue.GetForCurrentThread();
        _dispatcherTimer.Tick += QueryTimerCallback;
        _dispatcherTimer.Interval = new TimeSpan(0, 0, 5);
        _dispatcherTimer.Start();
        LoadSettings();
        LoadPictures();
    }

    public async void Dispose()
    {
        _dispatcherTimer.Stop();
        await _visionService.DisconnectAsync();
        await _visionService.UninitAsync();
    }

    [RelayCommand]
    private async Task StartCapture()
    {
        if (_visionService is not { IsInitialized: true, IsConnected: true })
        {
            return;
        }

        if (Temperature == "OFF")
        {
            return;
        }

        if (Calibrate.EndsWith("%"))
        {
            return;
        }

        var progress = new Progress<int>();
        progress.ProgressChanged += (sender, value) =>
        {
            _dispatcherQueue.EnqueueAsync( () =>
            {
                var profile = "Shooting".GetAppLocalized();
                if (value == 0)
                {
                    Shooting = profile;
                }
                else
                {
                    Shooting = profile + $" {value}%";
                }
            });
        };

        await _localSettingsService.SaveSettingAsync("OffsetX", OffsetX);
        await _localSettingsService.SaveSettingAsync("OffsetY", OffsetY);
        await _localSettingsService.SaveSettingAsync("ExposureTime", ExposureTime);

        // 在后台线程中执行 _visionService.StartCaptureAsync()
        var cts = new CancellationTokenSource();
        var timeout = TimeSpan.FromMilliseconds(10000 + ExposureTime * 5);

        var task = Task.Run(async () =>
        {
            if (cts.Token.IsCancellationRequested)
            {
                return;
            }

            var ox = _visionService.SetAttributeInt(OffsetX, "OffsetX");
            var oy = _visionService.SetAttributeInt(OffsetY, "OffsetY");
            if (ox && oy)
            {
                if (cts.Token.IsCancellationRequested)
                {
                    return;
                }
                await _visionService.ShootingAsync(progress, ExposureTime,cts.Token);
            }
            else
            {
                GlobalLog.Logger?.ReportError("Failed to set attribute.");
            }
        }, cts.Token);

        if (await Task.WhenAny(task, Task.Delay(timeout)) == task)
        {
            await Task.Run(LoadPictures);
        }
        else
        {
            cts.Cancel();
            GlobalLog.Logger?.ReportError("Failed to capture, timeout.");
        }
    }

    [RelayCommand]
    private async Task StartCalibrate()
    {
        if (_visionService is not { IsInitialized: true, IsConnected: true })
        {
            return;
        }

        if (Temperature == "OFF")
        {
            return;
        }

        if (Shooting.EndsWith("%"))
        {
            return;
        }

        var progress = new Progress<int>();
        progress.ProgressChanged += (sender, value) =>
        {
            _dispatcherQueue.EnqueueAsync( () =>
            {
                var profile = "Calibrate".GetAppLocalized();
                if (value == 0)
                {
                    Calibrate = profile;
                }
                else
                {
                    Calibrate = profile + $" {value}%";
                }
            });
        };
        
        var cts = new CancellationTokenSource();
        var timeout = TimeSpan.FromMilliseconds(1000 * 60);

        var task = Task.Run(async () =>
        {
            if (cts.Token.IsCancellationRequested)
            {
                return;
            }
            
            await _visionService.CalibrateAsync(progress);
            
        }, cts.Token);

        if (await Task.WhenAny(task, Task.Delay(timeout)) != task)
        {
            cts.Cancel();
            GlobalLog.Logger?.ReportError("Failed to calibrate, timeout.");
        }
    }
    
    [RelayCommand]
    private void Reverse()
    {
        var pictures = Pictures.Reverse().ToList();
        Pictures.Clear();
        foreach (var picture in pictures)
        {
            Pictures.Add(picture);
        }
    }

    [RelayCommand]
    private async Task OpenFolder()
    {
        var root =  await _localSettingsService.ReadSettingAsync<string>("Storage")
                              ?? Environment.GetFolderPath(Environment.SpecialFolder.MyDocuments);
        var folders = await _pictureService.GetFolderAsync();
        var path = await StorageFolder.GetFolderFromPathAsync(Path.Combine(root, folders.LastOrDefault() ?? string.Empty));
        if (path == null)
        {
            return;
        }
        
        _ = Launcher.LaunchFolderAsync(path);
    }

    public async void LoadPictures()
    {
        await _dispatcherQueue.EnqueueAsync(async () =>
        {
            Pictures.Clear();
            var folders = await _pictureService.GetFolderAsync();
            var first = folders.LastOrDefault();
            if (first == null)
            {
                return;
            }

            var pictures = await _pictureService.GetPicturesAsync(first);
            foreach (var picture in pictures.Reverse())
            {
                Pictures.Add(picture);
            }
        });
    }

    private async void LoadSettings()
    {
        var offsetX = await _localSettingsService.ReadSettingAsync<int>("OffsetX");
        var offsetY = await _localSettingsService.ReadSettingAsync<int>("OffsetY");
        var exposureTime = await _localSettingsService.ReadSettingAsync<int>("ExposureTime");
        OffsetX = offsetX;
        OffsetY = offsetY;
        ExposureTime = exposureTime;
    }

    private async void QueryTimerCallback(object? sender, object e)
    {
        if (!_visionService.IsInitialized)
        {
            await _visionService.InitAsync();
        }

        if (!_visionService.IsConnected)
        {
            await _visionService.ConnectAsync();
        }

        if (_visionService is not { IsInitialized: true, IsConnected: true })
        {
            return;
        }

        var temperature = _visionService.GetAttributeFloat("SensorTemperature");
        if (Math.Abs(temperature - -1.0) < 0.1)
        {
            Status = "\uF384";
            Temperature = "OFF";
        }
        else
        {
            Status = "\uE968";
            Temperature = $"{temperature} ℃";
        }
    }
}