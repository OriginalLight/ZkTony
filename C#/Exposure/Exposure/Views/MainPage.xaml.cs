using Exposure.Models;
using Exposure.ViewModels;
using Microsoft.UI.Xaml;
using Microsoft.UI.Xaml.Controls;
using Microsoft.UI.Xaml.Input;
using Microsoft.UI.Xaml.Media.Imaging;

namespace Exposure.Views;

public sealed partial class MainPage : Page
{
    public MainPage()
    {
        ViewModel = App.GetService<MainViewModel>();
        InitializeComponent();
        Image.ManipulationDelta += ImageToZoom_ManipulationDelta;
        Image.PointerWheelChanged += ImageToZoom_PointerWheelChanged;
    }

    public MainViewModel ViewModel
    {
        get;
    }

    private void OnItemClick(object sender, ItemClickEventArgs e)
    {
        var picture = (Picture)e.ClickedItem;
        Image.Source = new BitmapImage(new Uri(picture.Path));
        ImageTransform.ScaleX = 1;
        ImageTransform.ScaleY = 1;
        ImageTransform.TranslateX = 0;
        ImageTransform.TranslateY = 0;
        ImageTransform.Rotation = 0;
    }

    private void ImageToZoom_ManipulationDelta(object sender, ManipulationDeltaRoutedEventArgs e)
    {
        // 获取当前图像的缩放级别和平移
        var scale = ImageTransform.ScaleX * e.Delta.Scale;
        var translateX = ImageTransform.TranslateX + e.Delta.Translation.X;
        var translateY = ImageTransform.TranslateY + e.Delta.Translation.Y;

        // 控制缩放级别的最小值和最大值
        scale = Math.Max(0.5, Math.Min(2.0, scale)); // 限制缩放在0.5到2倍之间

        // 更新图像的缩放级别和平移
        ImageTransform.ScaleX = scale;
        ImageTransform.ScaleY = scale;
        ImageTransform.TranslateX = translateX;
        ImageTransform.TranslateY = translateY;
    }

    private void ImageToZoom_PointerWheelChanged(object sender, PointerRoutedEventArgs e)
    {
        var pointer = e.GetCurrentPoint(Image);

        // 计算缩放因子，这取决于鼠标滚轮的方向
        var scaleDelta = pointer.Properties.MouseWheelDelta > 0 ? 1.1 : 0.9;

        // 获取当前图像的缩放级别
        var scale = ImageTransform.ScaleX * scaleDelta;

        // 控制缩放级别的最小值和最大值
        scale = Math.Max(0.5, Math.Min(2.0, scale)); // 限制缩放在0.5到2倍之间

        // 更新图像的缩放级别
        ImageTransform.ScaleX = scale;
        ImageTransform.ScaleY = scale;

        // 阻止事件冒泡，以防止影响滚动条等其他控件
        e.Handled = true;
    }

    private void Rotate(object sender, RoutedEventArgs e) =>
        ImageTransform.Rotation = (ImageTransform.Rotation + 90) % 360;

    private void ZoomIn(object sender, RoutedEventArgs e)
    {
        var scale = ImageTransform.ScaleX * 1.1;
        scale = Math.Max(0.5, Math.Min(2.0, scale));
        ImageTransform.ScaleX = scale;
        ImageTransform.ScaleY = scale;
    }

    private void ZoomOut(object sender, RoutedEventArgs e)
    {
        var scale = ImageTransform.ScaleX * 0.9;
        scale = Math.Max(0.5, Math.Min(2.0, scale));
        ImageTransform.ScaleX = scale;
        ImageTransform.ScaleY = scale;
    }

    private void ReLoadPictures(object sender, RoutedEventArgs e) => ViewModel.LoadPictures();
}