using System.Collections.ObjectModel;

using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;

using Exposure.Contracts.Services;
using Exposure.Contracts.ViewModels;
using Exposure.Models;

namespace Exposure.ViewModels;

public partial class PictureViewModel : ObservableRecipient, INavigationAware
{
    private readonly INavigationService _navigationService;
    private readonly ISampleDataService _sampleDataService;
    private readonly IPictureService _pictureService;

    public ObservableCollection<SampleOrder> Source { get; } = new ObservableCollection<SampleOrder>();

    public PictureViewModel(INavigationService navigationService, ISampleDataService sampleDataService, IPictureService pictureService)
    {
        _navigationService = navigationService;
        _sampleDataService = sampleDataService;
        _pictureService = pictureService;
        _pictureService.LoadPicturesAsync();
    }

    public async void OnNavigatedTo(object parameter)
    {
        Source.Clear();

        // TODO: Replace with real data.
        var data = await _sampleDataService.GetContentGridDataAsync();
        foreach (var item in data)
        {
            Source.Add(item);
        }
    }

    public void OnNavigatedFrom()
    {
    }

    [RelayCommand]
    private void OnItemClick(SampleOrder? clickedItem)
    {
        if (clickedItem != null)
        {
            _navigationService.SetListDataItemForNextConnectedAnimation(clickedItem);
            _navigationService.NavigateTo(typeof(PictureDetailViewModel).FullName!, clickedItem.OrderID);
        }
    }
}
