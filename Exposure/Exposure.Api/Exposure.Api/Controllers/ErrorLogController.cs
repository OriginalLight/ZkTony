using System.Text;
using Exposure.Api.Contracts.Services;
using Exposure.Api.Models;
using Exposure.Api.Models.Dto;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Localization;
using Newtonsoft.Json;
using SqlSugar;

namespace Exposure.Api.Controllers;

[ApiController]
[Route("[controller]")]
public class ErrorLogController(
    IUsbService usb,
    IErrorLogService errorLog,
    IOperLogService operLog,
    IStringLocalizer<SharedResources> localizer) : ControllerBase
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
        return Ok(res);
    }

    #endregion

    #region 删除

    [HttpDelete]
    public async Task<IActionResult> Delete([FromBody] object[] ids)
    {
        if (!await errorLog.DeleteRange(ids)) return Problem(localizer.GetString("Delete").Value + localizer.GetString("Failure").Value);
        // 插入日志
        operLog.AddOperLog(localizer.GetString("Delete").Value, $"{localizer.GetString("ErrorLog").Value}：ids = {string.Join(",", ids)}");
        return Ok();
    }

    #endregion

    #region 导出

    [HttpPost]
    [Route("Export")]
    public async Task<IActionResult> Export([FromBody] object[] ids)
    {
        // 获取U盘
        var usb1 = usb.GetDefaultUsbDrive();
        if (usb1 == null) throw new Exception(localizer.GetString("NoUsb").Value);
        // 获取日志
        var list = await errorLog.GetByIds(ids);
        if (list.Count == 0) throw new Exception(localizer.GetString("NotFound").Value);
        // 保存到U盘
        await System.IO.File.WriteAllTextAsync(Path.Combine(usb1.Name, $"{localizer.GetString("ErrorLog").Value}.json"), JsonConvert.SerializeObject(list),
            Encoding.UTF8);
        // 插入日志
        operLog.AddOperLog(localizer.GetString("Export").Value, $"{localizer.GetString("ErrorLog").Value}：ids = " + string.Join(",", ids));
        // 返回结果
        return Ok();
    }

    #endregion
}