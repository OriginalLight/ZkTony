using Exposure.Api.Contracts.Services;
using Exposure.Api.Models.Dto;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Localization;
using OpenCvSharp;
using Serilog;
using SqlSugar;

namespace Exposure.Api.Controllers;

[ApiController]
[Route("[controller]")]
public class AlbumController(
    IAlbumService album,
    IOperLogService operLog,
    IUsbService usb,
    IAudioService audio,
    IStringLocalizer<SharedResources> localizer)
    : ControllerBase
{
    #region 分页查询

    [HttpPost]
    [Route("Page")]
    public async Task<IActionResult> Page([FromBody] AlbumQueryDto dto)
    {
        // 组合查询条件
        var total = new RefAsync<int>();
        var list = await album.GetByPage(dto, total);
        var res = new PageOutDto<List<AlbumOutDto>>
        {
            Total = total.Value,
            List = list
        };
        Log.Information("分页查询图集信息");
        return Ok(res);
    }

    #endregion

    #region 更新

    [HttpPut]
    public async Task<IActionResult> Update([FromBody] AlbumUpdateDto dto)
    {
        // 更新
        if (!await album.Update(dto))
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
        if (!await album.DeleteByIds(ids))
            throw new Exception(localizer.GetString("Delete").Value + localizer.GetString("Failure").Value);
        // 插入日志
        operLog.AddOperLog(localizer.GetString("Delete").Value,
            $"{localizer.GetString("Picture").Value}: {string.Join(',', ids)}");
        Log.Information("删除图片信息");
        return Ok();
    }

    #endregion

    #region 导出

    [HttpPost]
    [Route("Export")]
    public async Task<IActionResult> Export([FromBody] AlbumExportDto dto)
    {
        // 获取U盘
        var usb1 = usb.GetDefaultUsbDrive();
        if (usb1 == null) throw new Exception(localizer.GetString("NoUsb").Value);
        // 获取日志
        var list = await album.GetByIds(dto.Ids);
        if (list.Count == 0) throw new Exception(localizer.GetString("NotFound").Value);
        // 复制图片到U盘
        var path = Path.Combine(usb1.Name, localizer.GetString("Picture").Value);
        if (!Directory.Exists(path)) Directory.CreateDirectory(path);
        foreach (var albumOutDto in list)
        {
            var albumPath = Path.Combine(path, albumOutDto.Name);
            if (!Directory.Exists(albumPath)) Directory.CreateDirectory(albumPath);

            foreach (var photo in albumOutDto.Photos)
            {
                var mat = new Mat(photo.Path, ImreadModes.AnyDepth);
                if (dto.Format == "jpg")
                    // 转换成8位
                    mat = new Mat(photo.Path, ImreadModes.Grayscale);
                // 保存图片
                var filename = $"{photo.Name}.{dto.Format}";
                var fullPath = Path.Combine(albumPath, filename);
                mat.SaveImage(fullPath);
            }

            var originalPath = Path.Combine(albumPath, localizer.GetString("Original").Value);
            if (!Directory.Exists(originalPath)) Directory.CreateDirectory(originalPath);

            foreach (var photo in albumOutDto.Original)
            {
                var mat = new Mat(photo.Path, ImreadModes.AnyDepth);
                if (dto.Format == "jpg")
                    // 转换成8位
                    mat = new Mat(photo.Path, ImreadModes.Grayscale);
                // 保存图片
                var filename = $"{photo.Name}.{dto.Format}";
                var fullPath = Path.Combine(originalPath, filename);
                mat.SaveImage(fullPath);
            }
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
}