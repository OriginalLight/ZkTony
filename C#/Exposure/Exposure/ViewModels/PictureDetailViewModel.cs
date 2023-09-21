using CommunityToolkit.Mvvm.ComponentModel;
using Exposure.Contracts.Services;
using Exposure.Contracts.ViewModels;
using Exposure.Models;

namespace Exposure.ViewModels;

public partial class PictureDetailViewModel : ObservableRecipient, INavigationAware
{
    private readonly IPictureService _pictureService;

    [ObservableProperty]
    private Picture? item;

    public PictureDetailViewModel(IPictureService pictureService)
    {
        _pictureService = pictureService;
    }

    public void OnNavigatedTo(object parameter)
    {
        if (parameter is Picture pi)
        {
            Item = pi;
        }
    }

    public void OnNavigatedFrom()
    {
    }
}
