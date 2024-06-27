using Exposure.Api.Contracts.Services;
using Microsoft.AspNetCore.Mvc;
using Serilog;

namespace Exposure.Api.Controllers;

[ApiController]
[Route("[controller]")]
public class AudioController(IAudioService audioService) : ControllerBase
{
    #region 播放

    [HttpGet]
    [Route("Play")]
    public IActionResult Play([FromQuery] string key)
    {
        // 播放
        audioService.Play(key);
        Log.Information("播放:" + key);
        return Ok();
    }

    #endregion
}