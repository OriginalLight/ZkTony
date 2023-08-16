using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using Exposure.Contracts.Services;
using Microsoft.UI.Xaml;

namespace Exposure.ViewModels;

public partial class SettingsViewModel : ObservableRecipient
{
    private readonly ILocalSettingsService _localSettingsService;
    private readonly IThemeSelectorService _themeSelectorService;

    [ObservableProperty] private ElementTheme _elementTheme;

    [ObservableProperty] private string _storage;

    [ObservableProperty] private string _versionDescription;

    public SettingsViewModel(IThemeSelectorService themeSelectorService, ILocalSettingsService localSettingsService)
    {
        _localSettingsService = localSettingsService;
        _themeSelectorService = themeSelectorService;
        _elementTheme = _themeSelectorService.Theme;
        _storage = Environment.GetFolderPath(Environment.SpecialFolder.MyDocuments);
        _versionDescription = GetVersionDescription();
    }

    [RelayCommand]
    private async Task SwitchThemeAsync(ElementTheme elementTheme)
    {
        ElementTheme = elementTheme;
        await _themeSelectorService.SetThemeAsync(elementTheme);
    }
    
    public async Task GetStorageAsync()
    {
        Storage = await _localSettingsService.ReadSettingAsync<string>(nameof(Storage)) ??
                  Environment.GetFolderPath(Environment.SpecialFolder.MyDocuments);
    }

    public async Task SetStorageAsync(string path)
    {
        Storage = path;
        await _localSettingsService.SaveSettingAsync(nameof(Storage), path);
    }

    private static string GetVersionDescription()
    {
        var appInfoService = App.GetService<IAppInfoService>();
        var version = appInfoService.GetAppVersion();

        return $"{version.Major}.{version.Minor}.{version.Build}.{version.Revision}";
    }
}