using Exposure.Api.Contracts.Services;
using Microsoft.AspNetCore.Mvc;

namespace Exposure.Api.Controllers;

[ApiController]
[Route("[controller]")]
public class AudioController(IAudioService audioService, ILogger<AudioController> logger) : ControllerBase
{
    #region 播放

    [HttpGet]
    [Route("Play")]
    public IActionResult Play([FromQuery] string key)
    {
        // 播放
        audioService.Play(key);
        logger.LogInformation("Play:" + key);
        return Ok();
    }

    #endregion
}