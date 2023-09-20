using CommunityToolkit.Mvvm.ComponentModel;
using Exposure.Contracts.Services;
using Exposure.Contracts.ViewModels;

namespace Exposure.ViewModels;

public partial class MainViewModel : ObservableRecipient, INavigationAware
{
    private readonly IVisionService _visionService;

    [ObservableProperty] private string _versionText = "Unknow";

    public MainViewModel(IVisionService visionService)
    {
        _visionService = visionService;
        _visionService.InitAsync();
    }

    public void OnNavigatedTo(object parameter)
    {
        _versionText = _visionService.GetVisionText();
    }

    public void OnNavigatedFrom()
    {
    }
}