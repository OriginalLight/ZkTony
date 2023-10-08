using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using Exposure.Contracts.Services;
using Exposure.Contracts.ViewModels;
using Exposure.Models;

namespace Exposure.ViewModels;

public partial class PictureDetailViewModel : ObservableRecipient, INavigationAware
{
    private readonly IPictureService _pictureService;
    private readonly INavigationService _navigationService;

    [ObservableProperty] private Picture? _item;

    public PictureDetailViewModel(IPictureService pictureService, INavigationService navigationService)
    {
        _pictureService = pictureService;
        _navigationService = navigationService;
    }

    public void OnNavigatedTo(object parameter)
    {
        if (parameter is Picture pic)
        {
            Item = pic;
        }
    }

    public void OnNavigatedFrom()
    {
    }
}