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
    private CancellationTokenSource _cts;

    public CameraController(ICameraService camera, IErrorLogService errorLog, IOperLogService operLog)
    {
        _camera = camera;
        _errorLog = errorLog;
        _operLog = operLog;
    }

    [HttpGet]
    [Route("Init")]
    public async Task<IActionResult> Init()
    {
        // 初始化
        try
        {
            await _camera.InitializeAsync();
        }
        catch (Exception e)
        {
            _errorLog.AddErrorLog(e);
            return Problem($"{e.Message}");
        }

        return Ok("初始化成功");
    }

    [HttpGet]
    [Route("Preview")]
    public async Task<IActionResult> Preview()
    {
        // 预览
        try
        {
            var res = await _camera.PreviewAsync();
            _operLog.AddOperLog("预览", "预览成功");
            return Ok(res);
        }
        catch (Exception e)
        {
            _errorLog.AddErrorLog(e);
            return Problem($"{e.Message}");
        }
    }

    [HttpGet]
    [Route("Pixel")]
    public async Task<IActionResult> Pixel([FromQuery] int index)
    {
        // 设置像素
        try
        {
            await _camera.SetPixelAsync(uint.Parse(index.ToString()));
        }
        catch (Exception e)
        {
            _errorLog.AddErrorLog(e);
            return Problem($"{e.Message}");
        }

        return Ok("设置画质成功");
    }

    [HttpGet]
    [Route("Auto")]
    public async Task<IActionResult> Auto()
    {
        // 自动拍照
        try
        {
            _cts = new CancellationTokenSource();
            await _camera.TakeAutoPhotoAsync(_cts.Token);
            _operLog.AddOperLog("自动拍照", "自动拍照成功");
        }
        catch (Exception e)
        {
            _errorLog.AddErrorLog(e);
            return Problem($"{e.Message}");
        }

        return Ok("自动拍照成功");
    }

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

    [HttpGet]
    [Route("Cancel")]
    public async Task<IActionResult> Cancel()
    {
        // 取消拍照
        try
        {
            await _camera.CancelAsync();
            _operLog.AddOperLog("取消拍照", "取消拍照成功");
        }
        catch (Exception e)
        {
            _errorLog.AddErrorLog(e);
            return Problem($"{e.Message}");
        }

        return Ok("取消拍照成功");
    }

    [HttpGet]
    [Route("Cache")]
    public async Task<IActionResult> Cache()
    {
        // 获取温度
        try
        {
            var cache = await _camera.GetCache();
            return Ok(cache);
        }
        catch (Exception e)
        {
            _errorLog.AddErrorLog(e);
            return Problem($"{e.Message}");
        }
    }
}