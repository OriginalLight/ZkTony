using Exposure.ViewModels;

using Microsoft.UI.Xaml.Controls;

namespace Exposure.Views;

public sealed partial class PicturePage : Page
{
    public PictureViewModel ViewModel
    {
        get;
    }

    public PicturePage()
    {
        ViewModel = App.GetService<PictureViewModel>();
        InitializeComponent();
    }
}
