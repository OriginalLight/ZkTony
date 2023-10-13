using Windows.ApplicationModel;
using Windows.Storage;
using Windows.System;
using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using Exposure.Contracts.Services;
using Exposure.Logging;
using Microsoft.UI.Xaml;

namespace Exposure.ViewModels;

public partial class SettingsViewModel : ObservableRecipient
{
    private readonly ILocalSettingsService _localSettingsService;
    private readonly IThemeSelectorService _themeSelectorService;
    [ObservableProperty] private ElementTheme _elementTheme;
    [ObservableProperty] private string _storage;
    [ObservableProperty] private string _version;

    public SettingsViewModel(IThemeSelectorService themeSelectorService, ILocalSettingsService localSettingsService)
    {
        _localSettingsService = localSettingsService;
        _themeSelectorService = themeSelectorService;
        _elementTheme = _themeSelectorService.Theme;
        _storage = Environment.GetFolderPath(Environment.SpecialFolder.MyDocuments);
        _version = GetVersion();
    }

    [RelayCommand]
    private async Task SwitchThemeAsync(ElementTheme elementTheme)
    {
        ElementTheme = elementTheme;
        await _themeSelectorService.SetThemeAsync(elementTheme);
    }

    public async Task GetStorageAsync()
    {
        var doc = Environment.GetFolderPath(Environment.SpecialFolder.MyDocuments);
        Storage = await _localSettingsService.ReadSettingAsync<string>(nameof(Storage)) ?? doc;
    }


    public async Task SetStorageAsync(string path)
    {
        Storage = path;
        await _localSettingsService.SaveSettingAsync(nameof(Storage), path);
    }

    public void OpenLogsAsync()
    {
        var folder = ApplicationData.Current.TemporaryFolder;
        if (folder != null)
        {
            _ = Launcher.LaunchFolderAsync(folder);
        }
        else
        {
            GlobalLog.Logger?.ReportError("无法打开日志文件夹");
        }
    }

    private static string GetVersion()
    {
        var ver = App.GetService<IAppInfoService>().GetAppVersion();
        var version = $"{ver.Major}.{ver.Minor}.{ver.Build}.{ver.Revision}";
        var architecture = Package.Current.Id.Architecture.ToString();
#if DEBUG
        var buildConfiguration = "DEBUG";
#else
        var buildConfiguration = "RELEASE";
#endif
        return $"{version} | {architecture} | {buildConfiguration}";
    }
}