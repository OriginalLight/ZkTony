using Exposure.Models;

namespace Exposure.Contracts.Services;

public interface IPictureService
{
    string? SelectedFolder
    {
        get;
        set;
    }

    Task<IEnumerable<string>> GetFolderAsync();

    Task<IEnumerable<Picture>> GetPicturesAsync(string folder);
}