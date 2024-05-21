using Exposure.Api.Contracts.Services;
using Exposure.Api.Models.Dto;
using Microsoft.AspNetCore.Mvc;
using Serilog;

namespace Exposure.Api.Controllers;

[ApiController]
[Route("[controller]")]
public class TestController(
    ITestService service
) : ControllerBase
{
    #region 老化测试

    [HttpPost]
    [Route("AgingTest")]
    public IActionResult AgingTest([FromBody] TestAgingDto dto)
    {
        service.AgingTest(dto);
        Log.Information("老化测试成功");
        return Ok();
    }

    #endregion
}