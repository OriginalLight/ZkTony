﻿using Windows.ApplicationModel.DataTransfer;
using Windows.Storage.AccessCache;
using Windows.Storage.Pickers;
using Exposure.Contracts.Services;
using Exposure.ViewModels;
using Microsoft.UI.Xaml;
using Microsoft.UI.Xaml.Controls;
using Microsoft.Windows.AppNotifications.Builder;
using WinRT.Interop;

namespace Exposure.Views;

// TODO: Set the URL for your privacy policy by updating SettingsPage_PrivacyTermsLink.NavigateUri in Resources.resw.
public sealed partial class SettingsPage : Page
{
    public SettingsPage()
    {
        ViewModel = App.GetService<SettingsViewModel>();
        InitializeComponent();
    }

    public SettingsViewModel ViewModel
    {
        get;
    }

    private async void Page_Loaded(object sender, RoutedEventArgs e)
    {
        var selectedTheme = ViewModel.ElementTheme;
        foreach (var item in ThemeSelectionComboBox.Items)
        {
            var comboItem = item as ComboBoxItem;
            if (comboItem?.Tag is ElementTheme tag && tag == selectedTheme)
            {
                ThemeSelectionComboBox.SelectedValue = item;
                break;
            }
        }

        await ViewModel.GetStorageAsync();
    }

    private async void PickFolder(object sender, RoutedEventArgs e)
    {
        // Create a folder picker
        var openPicker = new FolderPicker();

        // Retrieve the window handle (HWND) of the current WinUI 3 window.
        var window = App.MainWindow;
        var hWnd = WindowNative.GetWindowHandle(window);

        // Initialize the folder picker with the window handle (HWND).
        InitializeWithWindow.Initialize(openPicker, hWnd);

        // Set options for your folder picker
        openPicker.SuggestedStartLocation = PickerLocationId.Desktop;
        openPicker.FileTypeFilter.Add("*");

        // Open the picker for the user to pick a folder
        var folder = await openPicker.PickSingleFolderAsync();
        if (folder != null)
        {
            StorageApplicationPermissions.FutureAccessList.AddOrReplace("PickedFolderToken", folder);
            await ViewModel.SetStorageAsync(folder.Path);
        }
    }

    private void Copy(object sender, RoutedEventArgs e)
    {
        var ver = ViewModel.Version;
        var data = new DataPackage
        {
            RequestedOperation = DataPackageOperation.Copy
        };
        data.SetText(ver);
        Clipboard.SetContentWithOptions(data,
            new ClipboardContentOptions { IsAllowedInHistory = true, IsRoamable = true });
        Clipboard.Flush();
        var builder = new AppNotificationBuilder();
        builder.AddText("已复制到剪贴板");
        builder.AddText(ver);
        App.GetService<IAppNotificationService>().Show(builder.BuildNotification().Payload);
    }
}