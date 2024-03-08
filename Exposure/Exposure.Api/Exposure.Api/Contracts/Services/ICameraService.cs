﻿using Exposure.Api.Models;

namespace Exposure.Api.Contracts.Services;

public interface ICameraService
{
    /// <summary>
    ///     初始化 0: 成功 1: 无设备 2: 打开失败
    /// </summary>
    /// <returns></returns>
    void Init();

    /// <summary>
    ///  停止
    /// </summary>
    void Stop();

    /// <summary>
    ///     设置像素
    /// </summary>
    /// <param name="index"></param>
    /// <returns></returns>
    void SetPixel(uint index);

    /// <summary>
    ///     获取温度
    /// </summary>
    /// <returns></returns>
    double GetTemperature();

    /// <summary>
    ///     预览
    /// </summary>
    /// <returns></returns>
    Task PreviewAsync();

    /// <summary>
    ///     自动拍照
    /// </summary>
    /// <param name="ctsToken"></param>
    /// <returns></returns>
    Task<long> TakeAutoPhotoAsync(CancellationToken ctsToken);

    /// <summary>
    ///     手动拍照
    /// </summary>
    /// <param name="exposure"></param>
    /// <param name="frame"></param>
    /// <param name="ctsToken"></param>
    /// <returns></returns>
    Task TakeManualPhotoAsync(int exposure, int frame, CancellationToken ctsToken);

    /// <summary>
    ///     取消拍照
    /// </summary>
    /// <returns></returns>
    void CancelTask();

    /// <summary>
    ///     获取缓存
    /// </summary>
    /// <returns></returns>
    Task<List<Picture>> GetCacheAsync();
    
    /// <summary>
    ///     老化测试
    /// </summary>
    /// <returns></returns>
    void AgingTest();

    /// <summary>
    ///     数据采集
    /// </summary>
    /// <param name="start"></param>
    /// <param name="interval"></param>
    /// <param name="number"></param>
    /// <returns></returns>
    Task Collect(int start, int interval, int number);

}