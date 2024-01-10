using System.Diagnostics;
using Windows.ApplicationModel.DataTransfer;
using Windows.Storage;
using Windows.Storage.Pickers;
using Exposure.Contracts.Services;
using Exposure.Logging;
using Exposure.ViewModels;
using Microsoft.UI.Xaml;
using Microsoft.UI.Xaml.Controls;
using Microsoft.UI.Xaml.Input;
using Microsoft.Windows.AppNotifications.Builder;
using WinRT.Interop;

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

    private void OnLoaded(object sender, RoutedEventArgs e)
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

    private async void Delete(object sender, RoutedEventArgs e)
    {
        var picture = ViewModel.Item;
        if (picture == null || !File.Exists(picture.Path))
        {
            return;
        }

        var dialog = new ContentDialog
        {
            Title = "删除图片",
            Content = $"确定删除 {picture.Name} ？",
            PrimaryButtonText = "删除",
            CloseButtonText = "取消",
            XamlRoot = App.MainWindow.Content.XamlRoot
        };

        dialog.PrimaryButtonClick += (_, _) =>
        {
            File.Delete(picture.Path);
            App.GetService<INavigationService>().GoBack();
            var builder = new AppNotificationBuilder();
            builder.AddText("已删除图片");
            builder.AddText(picture.Name);
            App.GetService<IAppNotificationService>().Show(builder.BuildNotification().Payload);
            GlobalLog.Logger?.ReportInfo($"删除 {picture.Path}");
        };
        await dialog.ShowAsync();
    }

    private async void Copy(object sender, RoutedEventArgs e)
    {
        var picture = ViewModel.Item;
        if (picture == null || !File.Exists(picture.Path))
        {
            return;
        }

        // 复制图片到剪贴板
        var imageFile = await StorageFile.GetFileFromPathAsync(picture.Path);
        var dataPackage = new DataPackage();
        dataPackage.SetStorageItems(new List<IStorageItem> { imageFile });
        Clipboard.SetContent(dataPackage);
        var builder = new AppNotificationBuilder();
        builder.AddText("已复制到剪贴板");
        builder.AddText(picture.Name);
        App.GetService<IAppNotificationService>().Show(builder.BuildNotification().Payload);
        GlobalLog.Logger?.ReportInfo($"复制 {picture.Path}");
    }

    private async void Export(object sender, RoutedEventArgs e)
    {
        var picture = ViewModel.Item;
        if (picture == null || !File.Exists(picture.Path))
        {
            return;
        }

        // 保存图片到指定位置
        var savePicker = new FileSavePicker
        {
            SuggestedStartLocation = PickerLocationId.PicturesLibrary,
            SuggestedFileName = picture.Name,
            FileTypeChoices =
            {
                { ".tiff", new List<string> { ".tiff" } },
                { ".png", new List<string> { ".png" } },
                { ".jpg", new List<string> { ".jpg" } }
            }
        };

        var hWnd = WindowNative.GetWindowHandle(App.MainWindow);
        InitializeWithWindow.Initialize(savePicker, hWnd);

        var file = await savePicker.PickSaveFileAsync();

        if (file == null)
        {
            return;
        }

        var button = sender as Button;

        if (button == null)
        {
            return;
        }

        var newDpi = int.Parse(button.Tag.ToString() ?? "100");

        ViewModel.SavePictureToFile(newDpi, file);
    }

    private void OpenWithPhoto(object sender, RoutedEventArgs e)
    {
        var picture = ViewModel.Item;
        if (picture == null || !File.Exists(picture.Path))
        {
            return;
        }

        Process.Start("explorer.exe", "/open, \"" + picture.Path + "\"");
        GlobalLog.Logger?.ReportInfo($"使用照片应用打开 {picture.Path}");
    }
}