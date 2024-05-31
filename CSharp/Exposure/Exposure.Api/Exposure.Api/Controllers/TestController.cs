using Exposure.Api.Contracts.Services;
using Exposure.Api.Models.Dto;
using Exposure.Utilities;
using Microsoft.AspNetCore.Mvc;
using OpenCvSharp;
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

    [HttpGet]
    [Route("Test1")]
    public IActionResult AvailableStorage()
    {
        var start = DateTime.Now;
        var pic = Path.Combine(FileUtils.Exposure, "240514141712442.png");
        var mat = new Mat(pic, ImreadModes.AnyDepth);
        var i = OpenCvUtils.Histogram(mat, 0.001);
        var dst = OpenCvUtils.LutLinearTransform(mat, 0, i, 0, 65535);
        //var path = Path.Combine(FileUtils.Exposure, "test1.png");
        //dst.SaveImage(path);
        var end = DateTime.Now;
        Log.Information($"耗时：{(end - start).TotalMilliseconds}ms");
        return Ok();
    }
}