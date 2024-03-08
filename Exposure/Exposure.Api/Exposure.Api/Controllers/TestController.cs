using Exposure.Api.Contracts.Services;
using Exposure.Api.Models.Dto;
using Microsoft.AspNetCore.Mvc;

namespace Exposure.Api.Controllers;

[ApiController]
[Route("[controller]")]
public class TestController : ControllerBase
{
    private readonly ITestService _testService;

    #region 构造函数

    public TestController(ITestService testService)
    {
        _testService = testService;
    }

    #endregion

    #region 老化测试

    [HttpPost]
    [Route("AgingTest")]
    public IActionResult AgingTest([FromBody] TestAgingDto dto)
    {
        try
        {
            _testService.AgingTest(dto);
        }
        catch (Exception e)
        {
            return Problem(e.Message);
        }

        return Ok();
    }

    #endregion
}