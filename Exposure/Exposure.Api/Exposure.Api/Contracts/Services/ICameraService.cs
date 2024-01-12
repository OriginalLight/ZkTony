using System.Net.WebSockets;

namespace Exposure.Api.Contracts.Services;

public interface ICameraService
{
    /// <summary>
    ///     初始化 0: 成功 1: 无设备 2: 打开失败
    /// </summary>
    /// <returns></returns>
    Task<bool> InitializeAsync(WebSocket webSocket);

    /// <summary>
    ///     拍照
    /// </summary>
    /// <param name="webSocket"></param>
    /// <returns></returns>
    Task TakePhotoAsync(WebSocket webSocket);


    /// <summary>
    ///     拍照多个
    /// </summary>
    /// <param name="webSocket"></param>
    /// <param name="size"></param>
    /// <returns></returns>
    Task TakeMultiplePhotoAsync(WebSocket webSocket, uint size);

    /// <summary>
    ///     设置曝光
    /// </summary>
    /// <param name="webSocket"></param>
    /// <param name="time"></param>
    /// <returns></returns>
    Task SetExposureAsync(WebSocket webSocket, uint time);

    /// <summary>
    ///     设置像素
    /// </summary>
    /// <param name="webSocket"></param>
    /// <param name="index"></param>
    /// <returns></returns>
    Task SetPixelAsync(WebSocket webSocket, uint index);

    /// <summary>
    ///     获取温度
    /// </summary>
    /// <returns></returns>
    double GetTemperature();
}