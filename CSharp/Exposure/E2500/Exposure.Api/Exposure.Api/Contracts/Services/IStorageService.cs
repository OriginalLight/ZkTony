namespace Exposure.Api.Contracts.Services;

public interface IStorageService
{
    /// <summary>
    ///     清理预览图
    /// </summary>
    /// <returns></returns>
    Task ClearStorageAsync();

    /// <summary>
    ///     存储空间检测
    /// </summary>
    /// <returns></returns>
    double AvailableStorage();
    /// <summary>
    ///     删除所有文件
    /// </summary>
    void DeleteAll();
}