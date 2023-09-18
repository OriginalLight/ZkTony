using CommunityToolkit.Mvvm.ComponentModel;
using Exposure.Contracts.Services;

namespace Exposure.ViewModels;

public partial class MainViewModel : ObservableRecipient
{
    private readonly IVisionService _visionService;

    [ObservableProperty] private string _versionText;
    
    public MainViewModel(IVisionService visionService)
    {
        _visionService = visionService;
        _visionService.InitAsync();
        _versionText = _visionService.GetVisionText() ?? "No text found";
    }
}