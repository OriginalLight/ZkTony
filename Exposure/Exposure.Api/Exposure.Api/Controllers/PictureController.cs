using System.Drawing;
using System.Drawing.Imaging;
using Exposure.Api.Contracts.Services;
using Exposure.Api.Models;
using Exposure.Api.Models.Dto;
using Exposure.Utilities;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Localization;
using Serilog;
using SqlSugar;

namespace Exposure.Api.Controllers;

[ApiController]
[Route("[controller]")]
public class PictureController(
    IPictureService picture,
    IOperLogService operLog,
    IUsbService usb,
    IAudioService audio,
    IStringLocalizer<SharedResources> localizer)
    : ControllerBase
{
    #region 分页查询

    [HttpPost]
    [Route("Page")]
    public async Task<IActionResult> Page([FromBody] PictureQueryDto dto)
    {
        // 组合查询条件
        var total = new RefAsync<int>();
        var list = await picture.GetByPage(dto, total);
        var res = new PageOutDto<List<Picture>>
        {
            Total = total.Value,
            List = list
        };
        Log.Information("分页查询图片信息");
        return Ok(res);
    }

    #endregion

    #region 更新

    [HttpPut]
    public async Task<IActionResult> Update([FromBody] PictureUpdateDto dto)
    {
        // 更新
        if (!await picture.Update(dto))
            throw new Exception(localizer.GetString("Update").Value + localizer.GetString("Failure").Value);
        // 插入日志
        operLog.AddOperLog(localizer.GetString("Update").Value, $"{localizer.GetString("Picture").Value}: {dto.Id}");
        Log.Information("更新图片信息");
        return Ok();
    }

    #endregion

    #region 删除

    [HttpDelete]
    public async Task<IActionResult> Delete([FromBody] object[] ids)
    {
        // 删除
        if (!await picture.DeleteByIds(ids))
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
    public async Task<IActionResult> Combine([FromBody] object[] ids)
    {
        var list = await picture.GetByIds(ids);
        if (list.Count is 0 or not 2) throw new Exception(localizer.GetString("NotFound").Value);
        if (list[0].Width != list[1].Width || list[0].Height != list[1].Height)
            return Problem(localizer.GetString("SizeInconsistency").Value);
        var res = await picture.Combine(list[0], list[1]);
        operLog.AddOperLog(localizer.GetString("Combine").Value,
            $"{localizer.GetString("Picture").Value}: {string.Join(',', ids)}");
        Log.Information("合成图片");
        return Ok(res);
    }

    #endregion

    #region 导出

    [HttpPost]
    [Route("Export")]
    public async Task<IActionResult> Export([FromBody] PictureExportDto dto)
    {
        // 获取U盘
        var usb1 = usb.GetDefaultUsbDrive();
        if (usb1 == null) throw new Exception(localizer.GetString("NoUsb").Value);
        // 获取日志
        var list = await picture.GetByIds(dto.Ids);
        if (list.Count == 0) throw new Exception(localizer.GetString("NotFound").Value);
        // 复制图片到U盘
        var path = Path.Combine(usb1.Name, localizer.GetString("Picture").Value);
        Directory.CreateDirectory(path);
        foreach (var pic in list)
        {
            var bitmap = new Bitmap(pic.Path);
            var imageFormat = dto.Format switch
            {
                "jpg" => ImageFormat.Jpeg,
                "png" => ImageFormat.Png,
                "tiff" => ImageFormat.Tiff,
                _ => ImageFormat.Tiff
            };

            // 保存图片
            var filename = $"{pic.Name}.{dto.Format}";
            var fullPath = Path.Combine(path, filename);
            var counter = 1;
            while (FileUtils.Exists(fullPath))
            {
                filename = $"{pic.Name}_{counter}.{dto.Format}";
                fullPath = Path.Combine(path, filename);
                counter++;
            }

            bitmap.Save(fullPath, imageFormat);
            
        }

        // 记录日志
        operLog.AddOperLog(localizer.GetString("Export").Value,
            $"{localizer.GetString("Picture").Value}：ids = " + string.Join(",", dto.Ids));
        audio.PlayWithSwitch("Export");
        // 返回结果
        Log.Information("导出图片");
        return Ok();
    }

    #endregion

    #region 调整

    [HttpPost]
    [Route("Adjust")]
    public async Task<IActionResult> Adjust([FromBody] PictureAdjustDto dto)
    {
        // 获取日志
        var res = await picture.Adjust(dto);
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