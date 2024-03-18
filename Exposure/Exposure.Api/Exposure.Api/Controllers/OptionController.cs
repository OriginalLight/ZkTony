using Exposure.Api.Contracts.Services;
using Exposure.Api.Models;
using Microsoft.AspNetCore.Mvc;

namespace Exposure.Api.Controllers;

[ApiController]
[Route("[controller]")]
public class OptionController(ILogger<OptionController> logger, IOptionService option) : ControllerBase
{
    #region 读取

    [HttpGet]
    public async Task<ActionResult> Get([FromQuery] string key)
    {
        // 读取
        var res = await option.GetOptionValueAsync(key) ?? "None";
        logger.LogInformation("读取配置：" + key + " = " + res);
        return Ok(res);
    }

    #endregion
    
    
    #region 设置
    
    [HttpPost]
    public async Task<ActionResult> Set([FromBody] Option dto)
    {
        // 设置
        var res = await option.SetOptionValueAsync(dto.Key, dto.Value);
        logger.LogInformation("设置配置：" + dto.Key + " = " + dto.Value);
        return Ok(res);
    }
    
    #endregion
}