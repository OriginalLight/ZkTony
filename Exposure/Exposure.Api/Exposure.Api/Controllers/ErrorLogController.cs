using System.Text;
using Exposure.Api.Contracts.Services;
using Exposure.Api.Core;
using Exposure.Api.Models;
using Microsoft.AspNetCore.Mvc;
using Newtonsoft.Json;

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

    [HttpGet]
    public async Task<HttpResult> Page([FromQuery] int page, [FromQuery] int size)
    {
        // 查询日志
        _operLog.Create("查询", $"查询崩溃日志: 页码 = {page}, 大小 = {size}");
        // 返回结果
        var list = await _errorLog.getPageList<ErrorLog>(page, size);
        var total = await _errorLog.Count();
        return HttpResult.Success("查询成功", new PageList<List<ErrorLog>>
        {
            Page = page,
            Size = size,
            Total = total,
            Data = list
        });
    }

    [HttpDelete]
    public async Task<HttpResult> Delete([FromBody] object[] ids)
    {
        // 删除日志
        _operLog.Create("删除", $"删除崩溃日志: {JsonConvert.SerializeObject(ids)}");
        // 返回结果
        return await _errorLog.DeleteRange(ids) ? HttpResult.Success("删除成功", null) : HttpResult.Fail("删除失败");
    }

    [HttpGet]
    [Route("Export")]
    public async Task<HttpResult> Export()
    {
        // 导出日志
        _operLog.Create("导出", "导出崩溃日志");
        // 获取U盘
        var usb = _usb.GetDefaultUsbDrive();
        if (usb == null) return HttpResult.Fail("导出失败: 未找到可用的U盘");
        // 获取日志
        var list = await _errorLog.getAll();
        if (list.Count == 0) return HttpResult.Fail("导出失败: 未找到日志");

        // 转换为json
        var json = JsonConvert.SerializeObject(list);
        // 保存到U盘
        var path = Path.Combine(usb.Name, "日志.json");
        await System.IO.File.WriteAllTextAsync(path, json, Encoding.UTF8);
        // 返回结果
        return HttpResult.Success("导出成功", null);
    }
}