using System.Data;
using System.Globalization;
using CommunityToolkit.Mvvm.ComponentModel;
using Exposure.Contracts.Services;
using System.Threading;
using Windows.System;

namespace Exposure.ViewModels;

public partial class MainViewModel : ObservableRecipient, IDisposable
{
    private readonly Timer _timer;
    private readonly IVisionService _visionService;

    [ObservableProperty] private string _versionText = "Unknow";

    public MainViewModel(IVisionService visionService)
    {
        _visionService = visionService;
        _timer = new Timer(QueryTimerCallback, null, 0, 5000);
    }
    
    private async void QueryTimerCallback(object? state)
    {
        await _visionService.InitAsync();
        await _visionService.ConnectAsync();
        var temp = _visionService.GetAttributeFloatAsync("SensorTemperature");
    }
    

    public async void Dispose()
    {
        await _timer.DisposeAsync();
        await _visionService.DisconnectAsync();
        await _visionService.UninitAsync();
    }
}