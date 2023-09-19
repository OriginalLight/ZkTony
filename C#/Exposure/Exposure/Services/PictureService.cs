using System.Text.RegularExpressions;
using Exposure.Contracts.Services;
using Exposure.Models;
using Logging;

namespace Exposure.Services;

public class PictureService : IPictureService
{
    private readonly ILocalSettingsService _localSettingsService;

    public IDictionary<DateTime, List<Picture>> PictureLibrary { get; private set; } = new Dictionary<DateTime, List<Picture>>();

    public PictureService(ILocalSettingsService localSettingsService)
    {
        _localSettingsService = localSettingsService;
    }

    public async Task LoadPicturesAsync()
    {
        var folder = await _localSettingsService.ReadSettingAsync<string>("Storage")
            ?? Environment.GetFolderPath(Environment.SpecialFolder.MyDocuments);
        if (folder == null)
        {
            GlobalLog.Logger?.ReportError("图片文件夹错误");
            return;
        }
        else
        {
            var folders = Directory.GetDirectories(folder);
            // 检查文件名是否符合日期格式
            var regex = new Regex(@"\d{4}-\d{2}-\d{2}");
            foreach (var f in folders)
            {
                if (Regex.IsMatch(f, regex.ToString()))
                {
                    var date = DateTime.Parse(Regex.Match(f, regex.ToString()).Value);
                    var pictures = Directory.GetFiles(f);
                    foreach (var p in pictures)
                    {
                        // 判断文件是否符合图片格式
                        if (!p.EndsWith(".jpg") && !p.EndsWith(".png"))
                        {
                            continue;
                        }

                        var picture = new Picture
                        {
                            Name = Path.GetFileName(p),
                            Path = f
                        };
                        if (PictureLibrary.ContainsKey(date))
                        {
                            PictureLibrary[date].Add(picture);
                        }
                        else
                        {
                            PictureLibrary.Add(date, new List<Picture> { picture });
                        }
                    }
                }
            }
        }
        PictureLibrary = PictureLibrary.OrderByDescending(p => p.Key).ToDictionary(p => p.Key, p => p.Value);
        GlobalLog.Logger?.ReportInfo("图片加载完成");
        // 打印图片信息
        foreach (var p in PictureLibrary)
        {
            GlobalLog.Logger?.ReportInfo(p.Key.ToString());
            foreach (var pic in p.Value)
            {
                GlobalLog.Logger?.ReportInfo(pic.GetFullPath());
            }
        }
        await Task.CompletedTask;
    }
}
