using Exposure.Api.Contracts.Services;
using Exposure.Api.Models.Dto;
using Microsoft.AspNetCore.Mvc;

namespace Exposure.Api.Controllers;

[ApiController]
[Route("[controller]")]
public class TestController(ILogger<TestController> logger, ITestService service) : ControllerBase
{
    #region 老化测试

    [HttpPost]
    [Route("AgingTest")]
    public IActionResult AgingTest([FromBody] TestAgingDto dto)
    {
        service.AgingTest(dto);
        logger.LogInformation("老化测试成功");
        return Ok();
    }

    #endregion
}