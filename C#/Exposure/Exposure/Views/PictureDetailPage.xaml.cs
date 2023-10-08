using System.Diagnostics;
using System.Drawing;
using System.Drawing.Imaging;
using Exposure.Contracts.Services;
using Exposure.ViewModels;
using Microsoft.UI.Xaml;
using Microsoft.UI.Xaml.Controls;
using Microsoft.UI.Xaml.Input;
using Windows.ApplicationModel.DataTransfer;
using Windows.Storage;
using Windows.Storage.Pickers;
using WinRT.Interop;
using Image = System.Drawing.Image;

namespace Exposure.Views;

public sealed partial class PictureDetailPage : Page
{
    public PictureDetailPage()
    {
        ViewModel = App.GetService<PictureDetailViewModel>();
        InitializeComponent();
        ImageItem.ManipulationDelta += ImageToZoom_ManipulationDelta;
        ImageItem.PointerWheelChanged += ImageToZoom_PointerWheelChanged;
    }

    public PictureDetailViewModel ViewModel
    {
        get;
    }

    private async void OnLoaded(object sender, RoutedEventArgs e)
    {
        ImageTransform.ScaleX = 0.8;
        ImageTransform.ScaleY = 0.8;
    }

    private void ImageToZoom_ManipulationDelta(object sender, ManipulationDeltaRoutedEventArgs e)
    {
        // 获取当前图像的缩放级别和平移
        var scale = ImageTransform.ScaleX * e.Delta.Scale;
        var translateX = ImageTransform.TranslateX + e.Delta.Translation.X;
        var translateY = ImageTransform.TranslateY + e.Delta.Translation.Y;

        // 控制缩放级别的最小值和最大值
        scale = Math.Max(0.5, Math.Min(5.0, scale)); // 限制缩放在0.5到2倍之间

        // 更新图像的缩放级别和平移
        ImageTransform.ScaleX = scale;
        ImageTransform.ScaleY = scale;
        ImageTransform.TranslateX = translateX;
        ImageTransform.TranslateY = translateY;
    }

    private void ImageToZoom_PointerWheelChanged(object sender, PointerRoutedEventArgs e)
    {
        var pointer = e.GetCurrentPoint(ImageItem);

        // 计算缩放因子，这取决于鼠标滚轮的方向
        var scaleDelta = pointer.Properties.MouseWheelDelta > 0 ? 1.3 : 0.8;

        // 获取当前图像的缩放级别
        var scale = ImageTransform.ScaleX * scaleDelta;

        // 控制缩放级别的最小值和最大值
        scale = Math.Max(0.5, Math.Min(5.0, scale)); // 限制缩放在0.5到2倍之间

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
        var scale = ImageTransform.ScaleX * 1.3;
        scale = Math.Max(0.5, Math.Min(5.0, scale));
        ImageTransform.ScaleX = scale;
        ImageTransform.ScaleY = scale;
    }

    private void ZoomOut(object sender, RoutedEventArgs e)
    {
        var scale = ImageTransform.ScaleX * 0.8;
        scale = Math.Max(0.5, Math.Min(5.0, scale));
        ImageTransform.ScaleX = scale;
        ImageTransform.ScaleY = scale;
    }

    private void Delete(object sender, RoutedEventArgs e)
    {
        if (ViewModel.Item == null)
        {
            return;
        }

        var path = ViewModel.Item.Path;
        if (!File.Exists(path))
        {
            return;
        }

        File.Delete(path);
        App.GetService<INavigationService>().GoBack();
    }

    private async void Copy(object sender, RoutedEventArgs e)
    {
        if (ViewModel.Item == null)
        {
            return;
        }

        var path = ViewModel.Item.Path;
        if (!File.Exists(path))
        {
            return;
        }

        // 复制图片到剪贴板
        var imageFile = await StorageFile.GetFileFromPathAsync(path);
        var dataPackage = new DataPackage();
        dataPackage.SetStorageItems(new List<IStorageItem> { imageFile });
        Clipboard.SetContent(dataPackage);
    }

    private async void Export(object sender, RoutedEventArgs e)
    {
        if (ViewModel.Item == null)
        {
            return;
        }

        var pic = ViewModel.Item;
        if (!File.Exists(pic.Path))
        {
            return;
        }

        // 保存图片到指定位置
        var savePicker = new FileSavePicker()
        {
            SuggestedStartLocation = PickerLocationId.PicturesLibrary,
            SuggestedFileName = pic.Name,
            FileTypeChoices =
            {
                { ".png", new List<string> { ".png" } },
                { ".jpg", new List<string> { ".jpg" } },
                { ".tiff", new List<string> { ".tiff" } }
            }
        };

        var hWnd = WindowNative.GetWindowHandle(App.MainWindow);
        InitializeWithWindow.Initialize(savePicker, hWnd);

        var file = await savePicker.PickSaveFileAsync();

        if (file == null)
        {
            return;
        }

        var imageFormat = file.FileType.ToLower() switch
        {
            ".png" => ImageFormat.Png,
            ".jpg" => ImageFormat.Jpeg,
            ".tiff" => ImageFormat.Tiff,
            _ => ImageFormat.Png
        };

        var button = sender as Button;

        if (button == null)
        {
            return;
        }

        var newDpi = int.Parse(button.Tag.ToString() ?? "100");
        var originalImage = new Bitmap(Image.FromFile(pic.Path));

        var newWidth = (int)(originalImage.Width * newDpi / originalImage.HorizontalResolution);
        var newHeight = (int)(originalImage.Height * newDpi / originalImage.VerticalResolution);

        // 创建一个新的 Bitmap，同时设置新的 DPI 和分辨率
        var newImage = new Bitmap(newWidth, newHeight, PixelFormat.Format32bppArgb);
        newImage.SetResolution(newDpi, newDpi);

        using (var graphics = Graphics.FromImage(newImage))
        {
            // 使用高质量的插值模式进行绘制
            graphics.InterpolationMode = System.Drawing.Drawing2D.InterpolationMode.HighQualityBicubic;
            graphics.SmoothingMode = System.Drawing.Drawing2D.SmoothingMode.HighQuality;

            // 绘制原始图像到新图像上
            graphics.DrawImage(originalImage, 0, 0, newWidth, newHeight);
        }

        // 保存新图像
        newImage.Save(file.Path, imageFormat);
    }

    private void OpenWithPhoto(object sender, RoutedEventArgs e)
    {
        if (ViewModel.Item == null)
        {
            return;
        }

        var path = ViewModel.Item.Path;
        if (!File.Exists(path))
        {
            return;
        }

        Process.Start("explorer.exe", "/open, \"" + path + "\"");
    }

    private void Chart3D(object sender, RoutedEventArgs e)
    {
        if (ViewModel.Item == null)
        {
            return;
        }

        if (!File.Exists(ViewModel.Item.Path))
        {
            return;
        }

        var navigationService = App.GetService<INavigationService>();
        navigationService.NavigateTo(typeof(ChartViewModel).FullName!, ViewModel.Item);
    }
}