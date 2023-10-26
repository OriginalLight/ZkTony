using System.Text.RegularExpressions;
using Exposure.Contracts.Services;
using Exposure.Models;

namespace Exposure.Services;

public class PictureService : IPictureService
{
    private readonly ILocalSettingsService _localSettingsService;

    public PictureService(ILocalSettingsService localSettingsService)
    {
        _localSettingsService = localSettingsService;
    }

    public string? SelectedFolder
    {
        get;
        set;
    }

    public async Task<IEnumerable<string>> GetFolderAsync()
    {
        var root = await _localSettingsService.ReadSettingAsync<string>("Storage")
                   ?? Environment.GetFolderPath(Environment.SpecialFolder.MyDocuments);
        var fs = Directory.GetDirectories(root);
        var regex = new Regex(@"\d{4}-\d{2}-\d{2}");
        return (from f in fs
            where Regex.IsMatch(f, regex.ToString())
            select Regex.Match(f, regex.ToString()).Value
            into folder
            where DateTime.Parse(folder) >= DateTime.Now.AddDays(-180)
            select folder).ToList();
    }

    public async Task<IEnumerable<Picture>> GetPicturesAsync(string? folder)
    {
        if (folder == null)
        {
            return new List<Picture>();
        }

        var root = await _localSettingsService.ReadSettingAsync<string>("Storage")
                   ?? Environment.GetFolderPath(Environment.SpecialFolder.MyDocuments);
        var path = Path.Combine(root, folder);
        if (!Directory.Exists(path))
        {
            return new List<Picture>();
        }

        var ps = Directory.GetFiles(path);
        return (from p in ps
            where p.EndsWith(".jpg") || p.EndsWith(".png") || p.EndsWith(".tiff")
            select new Picture { Name = Path.GetFileNameWithoutExtension(p), Path = p }).ToList();
    }
}