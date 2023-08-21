using System.Diagnostics;
using Exposure.ViewModels;
using Microsoft.UI.Xaml;
using Microsoft.UI.Xaml.Controls;

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

    private void Photos(object sender, RoutedEventArgs e)
    {
        var path = @"C:\Users\ThinkBook\Desktop\WindowIcon.png";
        if (!File.Exists(path))
        {
            return;
        }
        Process.Start("explorer.exe", "/open, \"" + path + "\"");
    }
}