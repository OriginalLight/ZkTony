namespace Exposure.Api.Contracts.Services;

public interface IAutoCleanService
{
    /// <summary>
    ///   清理预览图
    /// </summary>
    /// <returns></returns>
    Task CleanPreviewAsync();
}