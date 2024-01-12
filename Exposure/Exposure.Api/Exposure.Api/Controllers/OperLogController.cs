using Exposure.Api.Contracts.Services;
using Exposure.Api.Core;
using Microsoft.AspNetCore.Mvc;
using Newtonsoft.Json;

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
    ///     删除
    /// </summary>
    /// <param name="ids"></param>
    /// <returns></returns>
    [HttpDelete]
    public async Task<HttpResult> Delete([FromBody] object[] ids)
    {
        // 删除日志
        _operLog.AddOperLog("删除", $"删除操作日志: {JsonConvert.SerializeObject(ids)}");
        return await _operLog.DeleteRange(ids) ? HttpResult.Success("删除成功", null) : HttpResult.Fail("删除失败");
    }
}