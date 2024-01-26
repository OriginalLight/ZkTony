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
public class ErrorLogController : ControllerBase
{
    private readonly IErrorLogService _errorLog;
    private readonly IOperLogService _operLog;
    private readonly IUsbService _usb;

    public ErrorLogController(
        IUsbService usb,
        IErrorLogService errorLog,
        IOperLogService operLog)
    {
        _usb = usb;
        _errorLog = errorLog;
        _operLog = operLog;
    }

    /// <summary>
    ///     分页查询
    /// </summary>
    /// <param name="dto"></param>
    /// <returns></returns>
    [HttpPost]
    [Route("Page")]
    public async Task<IActionResult> Page([FromBody] ErrorLogQueryDto dto)
    {
        // 查询
        var total = new RefAsync<int>();
        var list = await _errorLog.GetByPage(dto, total);
        return new JsonResult(new PageOutDto<List<ErrorLog>>
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
        if (await _errorLog.DeleteRange(ids))
        {
            // 插入日志
            _operLog.AddOperLog("删除", $"删除崩溃日志：ids = {string.Join(",", ids)}");
            return Ok("删除成功");
        }

        // 返回结果
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
        var list = await _errorLog.GetByIds(ids);
        if (list.Count == 0) return Problem("未找到相关日志");
        // 保存到U盘
        await System.IO.File.WriteAllTextAsync(Path.Combine(usb.Name, "错误日志.json"), JsonConvert.SerializeObject(list),
            Encoding.UTF8);
        // 插入日志
        _operLog.AddOperLog("导出", "导出崩溃日志：ids = " + string.Join(",", ids));
        // 返回结果
        return Ok("导出成功");
    }
}