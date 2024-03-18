using Exposure.Api.Contracts.Services;
using Microsoft.AspNetCore.Mvc;

namespace Exposure.Api.Controllers;

[ApiController]
[Route("[controller]")]
public class CameraController(
    ILogger<CameraController> logger,
    ICameraService camera,
    IOperLogService operLog
    ) : ControllerBase
{
    private CancellationTokenSource? _cts;

    #region 初始化

    [HttpGet]
    [Route("Init")]
    public async Task<IActionResult> Init()
    {
        // 初始化
        await camera.InitAsync();
        logger.LogInformation("初始化成功");
        return Ok();
    }

    #endregion

    #region 预览

    [HttpGet]
    [Route("Preview")]
    public async Task<IActionResult> Preview()
    {
        // 预览
        await camera.PreviewAsync();
        operLog.AddOperLog("预览", "预览成功");
        logger.LogInformation("预览成功");
        return Ok();
    }

    #endregion

    #region 设置分辨率

    [HttpGet]
    [Route("Pixel")]
    public async Task<IActionResult> Pixel([FromQuery] int index)
    {
        // 设置像素
        await camera.SetPixel((uint)index);
        logger.LogInformation($"设置像素成功: {index}");
        return Ok();
    }

    #endregion

    #region 自动拍照

    [HttpGet]
    [Route("Auto")]
    public async Task<IActionResult> Auto()
    {
        // 自动拍照
        _cts = new CancellationTokenSource();
        var res = await camera.TakeAutoPhotoAsync(_cts.Token);
        operLog.AddOperLog("自动拍照", "自动拍照成功");
        logger.LogInformation("自动拍照成功");
        return Ok(res);
    }

    #endregion

    #region 手动拍照

    [HttpGet]
    [Route("Manual")]
    public async Task<IActionResult> Manual([FromQuery] int exposure, [FromQuery] int frame)
    {
        // 手动拍照
        _cts = new CancellationTokenSource();
        await camera.TakeManualPhotoAsync(exposure, frame, _cts.Token);
        operLog.AddOperLog("手动拍照", "手动拍照成功");
        logger.LogInformation("手动拍照成功");
        return Ok();
    }

    #endregion

    #region 取消拍照

    [HttpGet]
    [Route("Cancel")]
    public async Task<IActionResult> Cancel()
    {
        // 取消拍照
        await camera.CancelTask();
        operLog.AddOperLog("取消拍照", "取消拍照成功");
        logger.LogInformation("取消拍照成功");
        return Ok();
    }

    #endregion

    #region 获取缓存图片

    [HttpGet]
    [Route("Result")]
    public async Task<IActionResult> Result()
    {
        // 获取温度
        var cache = await camera.GetCacheAsync();
        logger.LogInformation("获取缓存图片成功");
        return Ok(cache);
    }

    #endregion

    #region 照片采集

    [HttpGet]
    [Route("Collect")]
    public async Task<IActionResult> Collect([FromQuery] int start, [FromQuery] int interval, [FromQuery] int number)
    {
        // 获取温度
        await camera.Collect(start, interval, number);
        logger.LogInformation("照片采集成功");
        return Ok();
    }

    #endregion
}