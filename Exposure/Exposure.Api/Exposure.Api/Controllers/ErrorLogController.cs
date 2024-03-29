using System.Text;
using Exposure.Api.Contracts.Services;
using Exposure.Api.Models;
using Exposure.Api.Models.Dto;
using Microsoft.AspNetCore.Mvc;
using Newtonsoft.Json;
using SqlSugar;

namespace Exposure.Api.Controllers;

[ApiController]
[Route("[controller]")]
public class ErrorLogController(
    ILogger<ErrorLogController> logger,
    IUsbService usb,
    IErrorLogService errorLog,
    IOperLogService operLog
) : ControllerBase
{
    #region 分页查询

    [HttpPost]
    [Route("Page")]
    public async Task<IActionResult> Page([FromBody] ErrorLogQueryDto dto)
    {
        // 查询
        var total = new RefAsync<int>();
        var list = await errorLog.GetByPage(dto, total);
        var res = new PageOutDto<List<ErrorLog>>
        {
            Total = total.Value,
            List = list
        };
        logger.LogInformation("分页查询成功");
        return Ok(res);
    }

    #endregion

    #region 删除

    [HttpDelete]
    public async Task<IActionResult> Delete([FromBody] object[] ids)
    {
        if (!await errorLog.DeleteRange(ids)) return Problem("删除失败");
        // 插入日志
        operLog.AddOperLog("删除", $"删除崩溃日志：ids = {string.Join(",", ids)}");
        logger.LogInformation("删除成功");
        return Ok("删除成功");
    }

    #endregion

    #region 导出

    [HttpPost]
    [Route("Export")]
    public async Task<IActionResult> Export([FromBody] object[] ids)
    {
        // 获取U盘
        var usb1 = usb.GetDefaultUsbDrive();
        if (usb1 == null) throw new Exception("未找到可用的U盘");
        // 获取日志
        var list = await errorLog.GetByIds(ids);
        if (list.Count == 0) throw new Exception("未找到相关日志");
        // 保存到U盘
        await System.IO.File.WriteAllTextAsync(Path.Combine(usb1.Name, "错误日志.json"), JsonConvert.SerializeObject(list),
            Encoding.UTF8);
        // 插入日志
        operLog.AddOperLog("导出", "导出崩溃日志：ids = " + string.Join(",", ids));
        logger.LogInformation("导出成功");
        // 返回结果
        return Ok("导出成功");
    }

    #endregion
}