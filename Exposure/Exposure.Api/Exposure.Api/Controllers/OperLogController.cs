using Exposure.Api.Contracts.Services;
using Exposure.Api.Models.Dto;
using Microsoft.AspNetCore.Mvc;
using MiniExcelLibs;
using SqlSugar;

namespace Exposure.Api.Controllers;

[ApiController]
[Route("[controller]")]
public class OperLogController : ControllerBase
{
    private readonly IOperLogService _operLog;
    private readonly IUsbService _usb;

    public OperLogController(IUsbService usb, IOperLogService operLog)
    {
        _usb = usb;
        _operLog = operLog;
    }
    
    /// <summary>
    ///  分页查询
    /// </summary>
    /// <param name="dto"></param>
    /// <returns></returns>
    [HttpPost]
    [Route("Page")]
    public async Task<IActionResult> Page([FromBody] OperLogQueryDto dto)
    {
        // 查询
        var total = new RefAsync<int>();
        var list = await _operLog.GetByPage(dto, total);
        return new JsonResult(new PageOutDto<List<OperLogOutDto>>
        {
            Total = total.Value,
            List = list
        });
    }
    
    /// <summary>
    ///     删除
    /// </summary>
    /// <param name="ids"></param>
    /// <returns></returns>
    [HttpDelete]
    public async Task<IActionResult> Delete([FromBody] object[] ids)
    {
        if (await _operLog.DeleteRange(ids))
        {
            // 插入日志
            _operLog.AddOperLog("删除", "删除操作日志：ids = " + string.Join(",", ids));
            return Ok("删除成功");
        }

        return Problem("删除失败");
    }

    /// <summary>
    ///     导出
    /// </summary>
    /// <returns></returns>
    [HttpPost]
    [Route("Export")]
    public async Task<IActionResult> Export([FromBody] object[] ids)
    {
        // 获取U盘
        var usb = _usb.GetDefaultUsbDrive();
        if (usb == null) return Problem("未找到可用的U盘");
        // 获取日志
        var list = await _operLog.GetByIds(ids);
        if (list.Count == 0) return Problem("未找到相关日志");

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
        await MiniExcel.SaveAsAsync(Path.Combine(usb.Name, "操作日志.xlsx"), data, overwriteFile: true);
        // 记录日志
        _operLog.AddOperLog("导出", "导出操作日志：ids = " + string.Join(",", ids));
        // 返回结果
        return Ok("导出成功");
    }
}