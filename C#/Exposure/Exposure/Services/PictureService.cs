using System.Text.RegularExpressions;
using Exposure.Contracts.Services;
using Exposure.Models;

namespace Exposure.Services;

public partial class PictureService : IPictureService
{
    private readonly ILocalSettingsService _localSettingsService;

    public PictureService(ILocalSettingsService localSettingsService)
    {
        _localSettingsService = localSettingsService;
    }

    public async Task<IEnumerable<string>> GetFolderAsync()
    {
        var path = await _localSettingsService.ReadSettingAsync<string>("Storage")
                   ?? Environment.GetFolderPath(Environment.SpecialFolder.MyDocuments);
        var fs = Directory.GetDirectories(path);
        var regex = DateRegex();
        return (from f in fs
            where Regex.IsMatch(f, regex.ToString())
            select Regex.Match(f, regex.ToString()).Value
            into folder
            where DateTime.Parse(folder) >= DateTime.Now.AddDays(-180)
            select folder).ToList();
    }

    public async Task<IEnumerable<Picture>> GetPicturesAsync(string folder)
    {
        var path = await _localSettingsService.ReadSettingAsync<string>("Storage")
                   ?? Environment.GetFolderPath(Environment.SpecialFolder.MyDocuments);
        var full = Path.Combine(path, folder);
        if (!Directory.Exists(full))
        {
            return new List<Picture>();
        }

        var ps = Directory.GetFiles(full);
        return (from p in ps
            where p.EndsWith(".jpg") || p.EndsWith(".png") || p.EndsWith(".tiff")
            select new Picture { Name = Path.GetFileNameWithoutExtension(p), Path = p }).ToList();
    }

    [GeneratedRegex("\\d{4}-\\d{2}-\\d{2}")]
    private static partial Regex DateRegex();
}