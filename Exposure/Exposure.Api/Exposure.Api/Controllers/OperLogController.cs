using Exposure.Api.Contracts.Services;
using Exposure.Api.Models.Dto;
using Microsoft.AspNetCore.Mvc;
using MiniExcelLibs;
using SqlSugar;

namespace Exposure.Api.Controllers;

[ApiController]
[Route("[controller]")]
public class OperLogController(ILogger<OperLogController> logger, IUsbService usb, IOperLogService operLog)
    : ControllerBase
{
    #region 分页查询

    [HttpPost]
    [Route("Page")]
    public async Task<IActionResult> Page([FromBody] OperLogQueryDto dto)
    {
        // 查询
        var total = new RefAsync<int>();
        var list = await operLog.GetByPage(dto, total);
        var res = new PageOutDto<List<OperLogOutDto>>
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
        if (!await operLog.DeleteRange(ids)) return Problem("删除失败");
        // 插入日志
        operLog.AddOperLog("删除", "删除操作日志：ids = " + string.Join(",", ids));
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
        var list = await operLog.GetByIds(ids);
        if (list.Count == 0) throw new Exception("未找到相关日志");

        // 使用MiniExcel导出
        var data = list.Select(p => new
        {
            编号 = p.Id,
            用户名 = p.User?.Name,
            操作类型 = p.Type,
            操作描述 = p.Description,
            操作时间 = p.Time
        }).ToList();
        // 保存到U盘
        await MiniExcel.SaveAsAsync(Path.Combine(usb1.Name, "操作日志.xlsx"), data, overwriteFile: true);
        // 记录日志
        operLog.AddOperLog("导出", "导出操作日志：ids = " + string.Join(",", ids));
        logger.LogInformation("导出成功");
        // 返回结果
        return Ok("导出成功");
    }

    #endregion
}