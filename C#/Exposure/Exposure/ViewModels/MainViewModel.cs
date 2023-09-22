using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using Exposure.Contracts.Services;
using DispatcherTimer = Microsoft.UI.Xaml.DispatcherTimer;

namespace Exposure.ViewModels;

public partial class MainViewModel : ObservableRecipient, IDisposable
{
    private readonly DispatcherTimer _dispatcherTimer;
    private readonly IVisionService _visionService;

    [ObservableProperty] private string _versionText = "Unknow";

    public MainViewModel(IVisionService visionService)
    {
        _visionService = visionService;
        _dispatcherTimer = new DispatcherTimer();
        _dispatcherTimer.Tick += QueryTimerCallback;
        _dispatcherTimer.Interval = new TimeSpan(0, 0, 5);
        _dispatcherTimer.Start();
    }
    
    [RelayCommand]
    private async Task StartCapture()
    {
        await _visionService.StartCaptureAsync();
    }
    private async void QueryTimerCallback(object? sender, object e)
    {
        await _visionService.InitAsync();
        await _visionService.ConnectAsync();
        var temp = _visionService.GetAttributeFloatAsync("SensorTemperature");
        var status = _visionService.GetAttributeIntAsync("DeviceStatus");
        VersionText = $"SensorTemperature: {temp}";
    }

    public async void Dispose()
    {
        _dispatcherTimer.Stop();
        await _visionService.DisconnectAsync();
        await _visionService.UninitAsync();
    }
}