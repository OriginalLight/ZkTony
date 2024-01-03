using Exposure.Api.Contracts.Services;
using Exposure.Api.Core;
using Exposure.Api.Models;
using Microsoft.AspNetCore.Mvc;
using Newtonsoft.Json;

namespace Exposure.Api.Controllers;

[ApiController]
[Route("[controller]")]
public class OperLogController : ControllerBase
{
    private readonly IUsbService _usb;
    private readonly IOperLogService _operLog;

    public OperLogController(IUsbService usb, IOperLogService operLog)
    {
        _usb = usb;
        _operLog = operLog;
    }

    [HttpGet]
    public async Task<HttpResult> Page([FromQuery] int page, [FromQuery] int size)
    {
        // 查询日志
        _operLog.Create("查询", $"查询操作日志: 页码 = {page}, 大小 = {size}");
        // 返回结果
        var list = await _operLog.getPageList<OperLog>(page, size);
        var total = await _operLog.Count();
        return HttpResult.Success("查询成功", new PageList<List<OperLog>>
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
        _operLog.Create("删除", $"删除操作日志: {JsonConvert.SerializeObject(ids)}");
        return await _operLog.DeleteRange(ids) ? HttpResult.Success("删除成功", null) : HttpResult.Fail("删除失败");
    }
}