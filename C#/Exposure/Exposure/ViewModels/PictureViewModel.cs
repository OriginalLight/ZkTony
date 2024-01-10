using System.Collections.ObjectModel;
using Windows.Storage;
using Windows.System;
using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using Exposure.Contracts.Services;
using Exposure.Contracts.ViewModels;
using Exposure.Models;

namespace Exposure.ViewModels;

public partial class PictureViewModel : ObservableRecipient, INavigationAware
{
    private readonly ILocalSettingsService _localSettingsService;
    private readonly IPictureService _pictureService;

    public PictureViewModel(IPictureService pictureService, ILocalSettingsService localSettingsService)
    {
        _pictureService = pictureService;
        _localSettingsService = localSettingsService;
    }

    public ObservableCollection<string> Folders
    {
        get;
    } = new();

    public ObservableCollection<Picture> Pictures
    {
        get;
    } = new();

    public async void OnNavigatedTo(object parameter)
    {
        Folders.Clear();
        Pictures.Clear();
        var folders = await _pictureService.GetFolderAsync();
        foreach (var folder in folders)
        {
            Folders.Add(folder);
        }

        var selected = _pictureService.SelectedFolder;
        if (selected != null && Folders.Contains(selected))
        {
            var pictures = await _pictureService.GetPicturesAsync(selected);
            foreach (var picture in pictures.Reverse())
            {
                Pictures.Add(picture);
            }
        }
        else
        {
            var first = Folders.LastOrDefault();
            if (first == null)
            {
                return;
            }

            var pictures = await _pictureService.GetPicturesAsync(first);
            _pictureService.SelectedFolder = first;
            foreach (var picture in pictures.Reverse())
            {
                Pictures.Add(picture);
            }
        }
    }

    public void OnNavigatedFrom()
    {
    }

    [RelayCommand]
    private async Task OpenFolder()
    {
        var root = await _localSettingsService.ReadSettingAsync<string>("Storage")
                   ?? Environment.GetFolderPath(Environment.SpecialFolder.MyDocuments);
        var folder = await GetSelectedFolder();
        var path = await StorageFolder.GetFolderFromPathAsync(Path.Combine(root, folder));
        if (path == null)
        {
            return;
        }

        _ = Launcher.LaunchFolderAsync(path);
    }

    public async Task OnFolderChanged(string folder)
    {
        Pictures.Clear();
        var pictures = await _pictureService.GetPicturesAsync(folder);
        foreach (var picture in pictures)
        {
            Pictures.Add(picture);
        }

        _pictureService.SelectedFolder = folder;
    }

    public async Task<string> GetSelectedFolder()
    {
        var selected = _pictureService.SelectedFolder;
        if (selected != null)
        {
            return selected;
        }

        var folders = await _pictureService.GetFolderAsync();
        var first = folders.LastOrDefault();
        return first ?? string.Empty;
    }
}