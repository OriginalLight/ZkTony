using CommunityToolkit.WinUI.UI.Animations;
using Exposure.Contracts.Services;
using Exposure.ViewModels;
using Microsoft.UI.Xaml.Controls;
using Microsoft.UI.Xaml.Navigation;

namespace Exposure.Views;

public sealed partial class PictureDetailPage : Page
{
    public PictureDetailPage()
    {
        ViewModel = App.GetService<PictureDetailViewModel>();
        InitializeComponent();
    }

    public PictureDetailViewModel ViewModel
    {
        get;
    }

    protected override void OnNavigatedTo(NavigationEventArgs e)
    {
        base.OnNavigatedTo(e);
        this.RegisterElementForConnectedAnimation("animationKeyContentGrid", ItemHero);
    }

    protected override void OnNavigatingFrom(NavigatingCancelEventArgs e)
    {
        base.OnNavigatingFrom(e);
        if (e.NavigationMode != NavigationMode.Back)
        {
            return;
        }

        var navigationService = App.GetService<INavigationService>();

        if (ViewModel.Item != null)
        {
            navigationService.SetListDataItemForNextConnectedAnimation(ViewModel.Item);
        }
    }
}