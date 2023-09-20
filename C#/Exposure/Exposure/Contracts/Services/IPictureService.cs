using Exposure.Models;

namespace Exposure.Contracts.Services;
public interface IPictureService
{
    Task<IEnumerable<string>> GetFolderAsync();

    Task<IEnumerable<Picture>> GetPicturesAsync(string folder);
}
