using Exposure.ViewModels;
using Microsoft.UI.Xaml.Controls;

namespace Exposure.Views;

public sealed partial class ChartPage : Page
{
    public ChartViewModel ViewModel
    {
        get;
    }

    public ChartPage()
    {
        ViewModel = App.GetService<ChartViewModel>();
        InitializeComponent();
    }
}