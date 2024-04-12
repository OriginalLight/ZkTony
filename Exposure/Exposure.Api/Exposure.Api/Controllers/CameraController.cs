using Exposure.Api.Contracts.Services;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Localization;
using Serilog;

namespace Exposure.Api.Controllers;

[ApiController]
[Route("[controller]")]
public class CameraController(
    ICameraService camera,
    IOperLogService operLog,
    IAudioService audio,
    IStringLocalizer<SharedResources> localizer) : ControllerBase
{
    private CancellationTokenSource? _cts;

    #region 初始化

    [HttpGet]
    [Route("Init")]
    public async Task<IActionResult> Init()
    {
        // 初始化
        await camera.InitAsync();
        Log.Information("初始化相机");
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
        operLog.AddOperLog(localizer.GetString("Preview").Value, localizer.GetString("Success").Value);
        Log.Information("预览");
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
        Log.Information($"设置分辨率: {index}");
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
        audio.PlayWithSwitch("Shot");
        var res = await camera.TakeAutoPhotoAsync(_cts.Token);
        operLog.AddOperLog(localizer.GetString("AutoShot").Value, localizer.GetString("Success").Value);
        Log.Information("自动拍照");
        return Ok(res);
    }

    #endregion

    #region 手动拍照

    [HttpGet]
    [Route("Manual")]
    public async Task<IActionResult> Manual([FromQuery] uint exposure, [FromQuery] int frame)
    {
        // 手动拍照
        _cts = new CancellationTokenSource();
        audio.PlayWithSwitch("Shot");
        await camera.TakeManualPhotoAsync(exposure, frame, _cts.Token);
        operLog.AddOperLog(localizer.GetString("ManualShot").Value, localizer.GetString("Success").Value);
        Log.Information("手动拍照");
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
        audio.PlayWithSwitch("CancelShot");
        operLog.AddOperLog(localizer.GetString("CancelShot").Value, localizer.GetString("Success").Value);
        Log.Information("取消拍照");
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
        Log.Information("获取缓存图片");
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
        Log.Information("照片采集");
        return Ok();
    }

    #endregion
}