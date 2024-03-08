using Exposure.Api.Contracts.Services;
using Microsoft.AspNetCore.Mvc;

namespace Exposure.Api.Controllers;

[ApiController]
[Route("[controller]")]
public class CameraController : ControllerBase
{
    private readonly ICameraService _camera;
    private readonly IErrorLogService _errorLog;
    private readonly IOperLogService _operLog;
    private CancellationTokenSource? _cts;

    #region 构造函数

    /// <inheritdoc />
    public CameraController(ICameraService camera, IErrorLogService errorLog, IOperLogService operLog)
    {
        _camera = camera;
        _errorLog = errorLog;
        _operLog = operLog;
    }

    #endregion

    #region 初始化

    [HttpGet]
    [Route("Init")]
    public IActionResult Init()
    {
        // 初始化
        try
        {
            _camera.Init();
        }
        catch (Exception e)
        {
            _errorLog.AddErrorLog(e);
            return Problem($"{e.Message}");
        }

        return Ok("初始化成功");
    }

    #endregion

    #region 预览

    [HttpGet]
    [Route("Preview")]
    public async Task<IActionResult> Preview()
    {
        // 预览
        try
        {
            await _camera.PreviewAsync();
            _operLog.AddOperLog("预览", "预览成功");
            return Ok();
        }
        catch (Exception e)
        {
            _errorLog.AddErrorLog(e);
            return Problem($"{e.Message}");
        }
    }

    #endregion

    #region 设置分辨率

    [HttpGet]
    [Route("Pixel")]
    public IActionResult Pixel([FromQuery] int index)
    {
        // 设置像素
        try
        {
            _camera.SetPixel((uint)index);
        }
        catch (Exception e)
        {
            _errorLog.AddErrorLog(e);
            return Problem($"{e.Message}");
        }

        return Ok("设置画质成功");
    }

    #endregion

    #region 自动拍照

    [HttpGet]
    [Route("Auto")]
    public async Task<IActionResult> Auto()
    {
        // 自动拍照
        try
        {
            _cts = new CancellationTokenSource();
            var res = await _camera.TakeAutoPhotoAsync(_cts.Token);
            _operLog.AddOperLog("自动拍照", "自动拍照成功");
            return Ok(new Dictionary<string, long> { { "exposure", res } });
        }
        catch (Exception e)
        {
            _errorLog.AddErrorLog(e);
            return Problem($"{e.Message}");
        }
    }

    #endregion

    #region 手动拍照

    [HttpGet]
    [Route("Manual")]
    public async Task<IActionResult> Manual([FromQuery] int exposure, [FromQuery] int frame)
    {
        // 手动拍照
        try
        {
            _cts = new CancellationTokenSource();
            await _camera.TakeManualPhotoAsync(exposure, frame, _cts.Token);
            _operLog.AddOperLog("手动拍照", "手动拍照成功");
        }
        catch (Exception e)
        {
            _errorLog.AddErrorLog(e);
            return Problem($"{e.Message}");
        }

        return Ok("手动拍照成功");
    }

    #endregion

    #region 取消拍照

    [HttpGet]
    [Route("Cancel")]
    public IActionResult Cancel()
    {
        // 取消拍照
        try
        {
            _camera.CancelTask();
            _operLog.AddOperLog("取消拍照", "取消拍照成功");
        }
        catch (Exception e)
        {
            _errorLog.AddErrorLog(e);
            return Problem($"{e.Message}");
        }

        return Ok("取消拍照成功");
    }

    #endregion

    #region 获取缓存图片

    [HttpGet]
    [Route("Result")]
    public async Task<IActionResult> Result()
    {
        // 获取温度
        try
        {
            var cache = await _camera.GetCacheAsync();
            return Ok(cache);
        }
        catch (Exception e)
        {
            _errorLog.AddErrorLog(e);
            return Problem($"{e.Message}");
        }
    }

    #endregion

    #region 照片采集

    [HttpGet]
    [Route("Collect")]
    public async Task<IActionResult> Collect([FromQuery] int start, [FromQuery] int interval, [FromQuery] int number)
    {
        // 获取温度
        try
        {
            await _camera.Collect(start, interval, number);
            return Ok();
        }
        catch (Exception e)
        {
            _errorLog.AddErrorLog(e);
            return Problem($"{e.Message}");
        }
    }

    #endregion
}