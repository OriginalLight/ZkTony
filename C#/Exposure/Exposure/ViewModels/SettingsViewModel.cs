using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using Exposure.Contracts.Services;
using Logging;
using Microsoft.UI.Xaml;
using Windows.ApplicationModel;
using Windows.ApplicationModel.DataTransfer;
using Windows.Storage;
using Windows.System;

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

    public Task CopyVersion()
    {
        try
        {
            var data = new DataPackage
            {
                RequestedOperation = DataPackageOperation.Copy
            };
            data.SetText(GetVersion());

            Clipboard.SetContentWithOptions(data, new ClipboardContentOptions() { IsAllowedInHistory = true, IsRoamable = true });
            Clipboard.Flush();
        }
        catch (Exception ex)
        {
            GlobalLog.Logger?.ReportError(ex.Message);
        }

        return Task.CompletedTask;
    }

    public async Task GetStorageAsync()
    {
        var Default = Environment.GetFolderPath(Environment.SpecialFolder.MyDocuments);
        Storage = await _localSettingsService.ReadSettingAsync<string>(nameof(Storage)) ?? Default;
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