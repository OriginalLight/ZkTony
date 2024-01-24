using Exposure.Api.Models;

namespace Exposure.Api.Contracts.Services;

public interface ICameraService
{
    /// <summary>
    ///     初始化 0: 成功 1: 无设备 2: 打开失败
    /// </summary>
    /// <returns></returns>
    Task InitializeAsync();

    /// <summary>
    ///     设置像素
    /// </summary>
    /// <param name="index"></param>
    /// <returns></returns>
    Task SetPixelAsync(uint index);

    /// <summary>
    ///     获取温度
    /// </summary>
    /// <returns></returns>
    Task<double> GetTemperature();

    /// <summary>
    ///   预览
    /// </summary>
    /// <returns></returns>
    Task<Picture> PreviewAsync();
    
    /// <summary>
    ///  自动拍照
    /// </summary>
    /// <param name="ctsToken"></param>
    /// <returns></returns>
    Task TakeAutoPhotoAsync(CancellationToken ctsToken);
    /// <summary>
    ///  手动拍照
    /// </summary>
    /// <param name="exposure"></param>
    /// <param name="frame"></param>
    /// <param name="ctsToken"></param>
    /// <returns></returns>
    Task TakeManualPhotoAsync(int exposure, int frame, CancellationToken ctsToken);
    /// <summary>
    ///  取消拍照
    /// </summary>
    /// <returns></returns>
    Task CancelAsync();

    /// <summary>
    ///   获取缓存
    /// </summary>
    /// <returns></returns>
    Task<List<Picture>> GetCache();
}