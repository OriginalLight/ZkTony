using Exposure.Api.Contracts.Services;
using Exposure.Api.Models;
using Exposure.Api.Models.Dto;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Localization;
using OpenCvSharp;
using Serilog;
using SqlSugar;

namespace Exposure.Api.Controllers;

[ApiController]
[Route("[controller]")]
public class PhotoController(
    IPhotoService photo,
    IOperLogService operLog,
    IAudioService audio,
    IStringLocalizer<SharedResources> localizer)
    : ControllerBase
{
    #region 更新

    [HttpPut]
    public async Task<IActionResult> Update([FromBody] PhotoUpdateDto dto)
    {
        // 更新
        if (!await photo.Update(dto))
            throw new Exception(localizer.GetString("Update").Value + localizer.GetString("Failure").Value);
        // 插入日志
        operLog.AddOperLog(localizer.GetString("Update").Value, $"{localizer.GetString("Picture").Value}: {dto.Id}");
        Log.Information("更新图片信息");
        return Ok();
    }

    #endregion

    #region 删除

    [HttpDelete]
    public async Task<IActionResult> Delete([FromBody] int[] ids)
    {
        // 删除
        if (!await photo.DeleteByIds(ids))
            throw new Exception(localizer.GetString("Delete").Value + localizer.GetString("Failure").Value);
        // 插入日志
        operLog.AddOperLog(localizer.GetString("Delete").Value,
            $"{localizer.GetString("Picture").Value}: {string.Join(',', ids)}");
        Log.Information("删除图片信息");
        return Ok();
    }

    #endregion

    #region 合成

    [HttpPost]
    [Route("Combine")]
    public async Task<IActionResult> Combine([FromBody] int[] ids)
    {
        var res = await photo.Combine(ids);
        operLog.AddOperLog(localizer.GetString("Combine").Value,
            $"{localizer.GetString("Picture").Value}: {string.Join(',', ids)}");
        Log.Information("合成图片");
        return Ok(res);
    }

    #endregion

    #region 调整

    [HttpPost]
    [Route("Adjust")]
    public async Task<IActionResult> Adjust([FromBody] PhotoAdjustDto dto)
    {
        // 获取日志
        var res = await photo.Adjust(dto);
        if (dto.Code == 0)
        {
            audio.PlayWithSwitch("Save");
            operLog.AddOperLog(localizer.GetString("Adjust").Value,
                $"{localizer.GetString("Picture").Value}：id = " + dto.Id);
        }

        Log.Information("调整图片");
        return Ok(res);
    }

    #endregion
}