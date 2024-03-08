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

    #region 构造函数

    /// <inheritdoc />
    public ErrorLogController(IUsbService usb, IErrorLogService errorLog, IOperLogService operLog)
    {
        _usb = usb;
        _errorLog = errorLog;
        _operLog = operLog;
    }

    #endregion

    #region 分页查询

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

    #endregion

    #region 删除

    [HttpDelete]
    public async Task<IActionResult> Delete([FromBody] object[] ids)
    {
        if (!await _errorLog.DeleteRange(ids)) return Problem("删除失败");
        // 插入日志
        _operLog.AddOperLog("删除", $"删除崩溃日志：ids = {string.Join(",", ids)}");
        return Ok("删除成功");
    }

    #endregion

    #region 导出

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

    #endregion
}