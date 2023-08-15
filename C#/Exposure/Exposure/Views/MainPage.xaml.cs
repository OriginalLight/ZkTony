using Exposure.ViewModels;

namespace Exposure.Views;

public sealed partial class MainPage : Page
{
    public MainPage()
    {
        ViewModel = App.GetService<MainViewModel>();
        InitializeComponent();
    }

    public MainViewModel ViewModel
    {
        get;
    }
}