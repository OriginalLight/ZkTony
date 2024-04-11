namespace Exposure.Api.Contracts.Services;

public interface IStorageService
{
    /// <summary>
    ///     清理预览图
    /// </summary>
    /// <returns></returns>
    Task CleanPreviewAsync();

    /// <summary>
    ///     存储空间检测
    /// </summary>
    /// <returns></returns>
    double AvailableStorage();
}