using Exposure.Api.Contracts.Services;
using Exposure.Api.Models.Dto;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Localization;
using MiniExcelLibs;
using SqlSugar;

namespace Exposure.Api.Controllers;

[ApiController]
[Route("[controller]")]
public class OperLogController(
    IUsbService usb,
    IOperLogService operLog,
    IStringLocalizer<SharedResources> localizer)
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
        return Ok(res);
    }

    #endregion

    #region 删除

    [HttpDelete]
    public async Task<IActionResult> Delete([FromBody] object[] ids)
    {
        if (!await operLog.DeleteRange(ids))
            return Problem(localizer.GetString("Delete").Value + localizer.GetString("Failure").Value);
        // 插入日志
        operLog.AddOperLog(localizer.GetString("Delete").Value,
            $"{localizer.GetString("OperLog").Value}：ids = {string.Join(",", ids)}");
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
        var list = await operLog.GetByIds(ids);
        if (list.Count == 0) throw new Exception(localizer.GetString("NotFound").Value);

        // 判断使用中文还是英文
        var lang = HttpContext.Request.Headers["Accept-Language"].ToString().ToLower();
        if (lang.Contains("zh"))
        {
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
            await MiniExcel.SaveAsAsync(Path.Combine(usb1.Name, $"{localizer.GetString("OperLog").Value}.xlsx"), data,
                overwriteFile: true);
        }
        else
        {
            // 使用MiniExcel导出
            var data = list.Select(p => new
            {
                p.Id,
                UserName = p.User?.Name,
                p.Type,
                p.Description,
                p.Time
            }).ToList();
            // 保存到U盘
            await MiniExcel.SaveAsAsync(Path.Combine(usb1.Name, $"{localizer.GetString("OperLog").Value}.xlsx"), data,
                overwriteFile: true);
        }

        // 记录日志
        operLog.AddOperLog(localizer.GetString("Export").Value,
            $"{localizer.GetString("OperLog").Value}：ids = " + string.Join(",", ids));
        // 返回结果
        return Ok();
    }

    #endregion
}