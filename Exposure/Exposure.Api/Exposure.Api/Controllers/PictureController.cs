using System.Drawing;
using System.Drawing.Imaging;
using Exposure.Api.Contracts.Services;
using Exposure.Api.Models;
using Exposure.Api.Models.Dto;
using Microsoft.AspNetCore.Mvc;
using SqlSugar;

namespace Exposure.Api.Controllers;

[ApiController]
[Route("[controller]")]
public class PictureController : ControllerBase
{
    private readonly IOperLogService _operLog;
    private readonly IPictureService _picture;
    private readonly IUsbService _usb;

    #region 构造函数

    /// <inheritdoc />
    public PictureController(IPictureService picture, IOperLogService operLog, IUsbService usb)
    {
        _usb = usb;
        _picture = picture;
        _operLog = operLog;
    }

    #endregion

    #region 分页查询

    [HttpPost]
    [Route("Page")]
    public async Task<IActionResult> Page([FromBody] PictureQueryDto dto)
    {
        // 组合查询条件
        var total = new RefAsync<int>();
        var list = await _picture.GetByPage(dto, total);
        return new JsonResult(new PageOutDto<List<Picture>>
        {
            Total = total.Value,
            List = list
        });
    }

    #endregion

    #region 添加

    [HttpPut]
    public async Task<IActionResult> Update([FromBody] PictureUpdateDto dto)
    {
        // 更新
        if (!await _picture.Update(dto)) return Problem("更新失败");
        // 插入日志
        _operLog.AddOperLog("更新", $"更新照片: {dto.Id}");
        return Ok("更新成功");
    }

    #endregion

    #region 删除

    [HttpDelete]
    public async Task<IActionResult> Delete([FromBody] object[] ids)
    {
        // 删除
        if (!await _picture.DeleteRange(ids)) return Problem("删除失败");
        // 插入日志
        _operLog.AddOperLog("删除", $"删除照片: {string.Join(',', ids)}");
        return Ok("删除成功");
    }

    #endregion

    #region 合成

    [HttpPost]
    [Route("Combine")]
    public async Task<IActionResult> Combine([FromBody] object[] ids)
    {
        try
        {
            var list = await _picture.GetByIds(ids);
            if (list.Count is 0 or not 2) return Problem("未找到相关图片");
            if (list[0].Width != list[1].Width || list[0].Height != list[1].Height) return Problem("图片尺寸不一致");
            var res = await _picture.Combine(list);
            _operLog.AddOperLog("合并", $"合并照片: {string.Join(',', ids)}");
            return Ok(res);
        }
        catch (Exception e)
        {
            return Problem(e.Message);
        }
    }

    #endregion

    #region 导出

    [HttpPost]
    [Route("Export")]
    public async Task<IActionResult> Export([FromBody] PictureExportDto dto)
    {
        // 获取U盘
        var usb = _usb.GetDefaultUsbDrive();
        if (usb == null) return Problem("未找到可用的U盘");
        // 获取日志
        var list = await _picture.GetByIds(dto.Ids);
        if (list.Count == 0) return Problem("未找到相关图片");
        // 复制图片到U盘
        try
        {
            var path = Path.Combine(usb.Name, DateTime.Now.ToString("yyyyMMddHHmmss"));
            Directory.CreateDirectory(path);
            foreach (var picture in list)
            {
                var bitmap = new Bitmap(picture.Path);
                var imageFormat = dto.Format switch
                {
                    "jpg" => ImageFormat.Jpeg,
                    "png" => ImageFormat.Png,
                    "tiff" => ImageFormat.Tiff,
                    _ => ImageFormat.Tiff
                };
                bitmap.Save(Path.Combine(path, $"{picture.Name}.{dto.Format}"), imageFormat);
                // 释放资源
                bitmap.Dispose();
            }
        }
        catch (Exception e)
        {
            return Problem(e.Message);
        }

        // 记录日志
        _operLog.AddOperLog("导出", "导出图片：ids = " + string.Join(",", dto.Ids));
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
        var pic = await _picture.GetByPrimary(dto.Id);
        if (pic == null) return Problem("未找到相关图片");
        try
        {
            var res = await _picture.Adjust(pic, dto);
            _operLog.AddOperLog("调整", "调整图片：id = " + dto.Id);
            return Ok(res);
        }
        catch (Exception e)
        {
            return Problem(e.Message);
        }
    }

    #endregion
}