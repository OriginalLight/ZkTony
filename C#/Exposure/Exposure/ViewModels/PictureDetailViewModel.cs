using System.Drawing;
using System.Drawing.Drawing2D;
using System.Drawing.Imaging;
using Windows.Storage;
using CommunityToolkit.Mvvm.ComponentModel;
using Exposure.Contracts.Services;
using Exposure.Contracts.ViewModels;
using Exposure.Logging;
using Exposure.Models;
using Microsoft.Windows.AppNotifications.Builder;

namespace Exposure.ViewModels;

public partial class PictureDetailViewModel : ObservableRecipient, INavigationAware
{
    private readonly IAppNotificationService _appNotificationService;
    [ObservableProperty] private Picture? _item;
    
    public PictureDetailViewModel(IAppNotificationService appNotificationService)
    {
        _appNotificationService = appNotificationService;
    }

    public void OnNavigatedTo(object parameter)
    {
        if (parameter is Picture pic)
        {
            Item = pic;
        }
    }

    public void OnNavigatedFrom()
    {
    }


    public void SavePictureToFile(int newDpi, StorageFile file) =>
        Task.Run(() =>
        {
            if (Item == null || !File.Exists(Item.Path))
            {
                return;
            }

            var startTime = DateTime.Now;

            var originalImage = new Bitmap(Image.FromFile(Item.Path));

            var newWidth = (int)(originalImage.Width * newDpi / originalImage.HorizontalResolution);
            var newHeight = (int)(originalImage.Height * newDpi / originalImage.VerticalResolution);

            // 创建一个新的 Bitmap，同时设置新的 DPI 和分辨率
            var newImage = new Bitmap(newWidth, newHeight, PixelFormat.Format32bppArgb);
            newImage.SetResolution(newDpi, newDpi);

            using (var graphics = Graphics.FromImage(newImage))
            {
                // 使用高质量的插值模式进行绘制
                graphics.InterpolationMode = InterpolationMode.HighQualityBicubic;
                graphics.SmoothingMode = SmoothingMode.HighQuality;

                // 绘制原始图像到新图像上
                graphics.DrawImage(originalImage, 0, 0, newWidth, newHeight);
            }

            var imageFormat = file.FileType.ToLower() switch
            {
                ".png" => ImageFormat.Png,
                ".jpg" => ImageFormat.Jpeg,
                ".tiff" => ImageFormat.Tiff,
                _ => ImageFormat.Png
            };

            // 保存新图像
            newImage.Save(file.Path, imageFormat);
            var endTime = DateTime.Now;
            var timeSpan = endTime - startTime;
            var builder = new AppNotificationBuilder();
            builder.AddText($"导出完成：{timeSpan.TotalSeconds} 秒");
            builder.AddText(file.Name);
            _appNotificationService.Show(builder.BuildNotification().Payload);
            GlobalLog.Logger?.ReportInfo($"导出到 {file.Path}");
        });
}