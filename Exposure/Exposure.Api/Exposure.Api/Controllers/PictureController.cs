using System.Drawing;
using System.Drawing.Imaging;
using Exposure.Api.Contracts.Services;
using Exposure.Api.Models;
using Exposure.Api.Models.Dto;
using Exposure.Api.Utils;
using Microsoft.AspNetCore.Mvc;
using SqlSugar;

namespace Exposure.Api.Controllers;

[ApiController]
[Route("[controller]")]
public class PictureController(ILogger<PictureController> logger, IPictureService picture, IOperLogService operLog, IUsbService usb)
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
        logger.LogInformation("分页查询成功");
        return Ok(res);
    }

    #endregion

    #region 添加

    [HttpPut]
    public async Task<IActionResult> Update([FromBody] PictureUpdateDto dto)
    {
        // 更新
        if (!await picture.Update(dto)) throw new Exception("更新失败");
        // 插入日志
        operLog.AddOperLog("更新", $"更新照片: {dto.Id}");
        return Ok("更新成功");
    }

    #endregion

    #region 删除

    [HttpDelete]
    public async Task<IActionResult> Delete([FromBody] object[] ids)
    {
        // 删除
        if (!await picture.DeleteRange(ids)) throw new Exception("删除失败");
        // 插入日志
        operLog.AddOperLog("删除", $"删除照片: {string.Join(',', ids)}");
        logger.LogInformation("删除成功");
        return Ok("删除成功");
    }

    #endregion

    #region 合成

    [HttpPost]
    [Route("Combine")]
    public async Task<IActionResult> Combine([FromBody] object[] ids)
    {
        var list = await picture.GetByIds(ids);
        if (list.Count is 0 or not 2) throw new Exception("未找到相关图片");
        if (list[0].Width != list[1].Width || list[0].Height != list[1].Height) return Problem("图片尺寸不一致");
        var res = await picture.Combine(list);
        operLog.AddOperLog("合并", $"合并照片: {string.Join(',', ids)}");
        logger.LogInformation("合并成功");
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
        if (usb1 == null) throw new Exception("未找到可用的U盘");
        // 获取日志
        var list = await picture.GetByIds(dto.Ids);
        if (list.Count == 0) throw new Exception("未找到相关图片");
        // 复制图片到U盘
        var path = Path.Combine(usb1.Name, DateTime.Now.ToString("yyyyMMddHHmmss"));
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

            // 释放资源
            bitmap.Dispose();
        }

        // 记录日志
        operLog.AddOperLog("导出", "导出图片：ids = " + string.Join(",", dto.Ids));
        // 返回结果
        return Ok("导出成功");
    }

    #endregion

    #region 调整

    [HttpPost]
    [Route("Adjust")]
    public async Task<IActionResult> Adjust([FromBody] PictureAdjustDto dto)
    {
        // 获取日志
        var res = await picture.Adjust(dto);
        operLog.AddOperLog("调整", "调整图片：id = " + dto.Id);
        return Ok(res);
    }

    #endregion
}