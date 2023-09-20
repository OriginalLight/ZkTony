using System.Collections.ObjectModel;

using CommunityToolkit.Mvvm.ComponentModel;

using Exposure.Contracts.Services;
using Exposure.Contracts.ViewModels;
using Exposure.Models;

namespace Exposure.ViewModels;

public partial class PictureViewModel : ObservableRecipient, INavigationAware
{
    private readonly IPictureService _pictureService;

    public ObservableCollection<string> Folders { get; } = new();
    public ObservableCollection<Picture> Pictures { get; } = new();

    public PictureViewModel(IPictureService pictureService)
    {
        _pictureService = pictureService;
    }

    public async void OnNavigatedTo(object parameter)
    {
        Folders.Clear();
        Pictures.Clear();
        var folders = await _pictureService.GetFolderAsync();
        foreach (var folder in folders)
        {
            Folders.Add(folder);
        }
        var first = Folders.LastOrDefault();
        if (first == null)
        {
            return;
        }

        var pictures = await _pictureService.GetPicturesAsync(first);
        foreach (var picture in pictures)
        {
            Pictures.Add(picture);
        }
    }

    public void OnNavigatedFrom()
    {
    }

    public async Task OnFolderChanged(string folder)
    {
        Pictures.Clear();
        var pictures = await _pictureService.GetPicturesAsync(folder);
        foreach (var picture in pictures)
        {
            Pictures.Add(picture);
        }
    }
}