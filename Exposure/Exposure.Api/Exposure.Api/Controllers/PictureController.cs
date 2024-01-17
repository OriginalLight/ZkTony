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

    public PictureController(IPictureService picture, IOperLogService operLog)
    {
        _picture = picture;
        _operLog = operLog;
    }

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

    [HttpPut]
    public async Task<IActionResult> Update([FromBody] Picture dto)
    {
        // 更新
        if (await _picture.Update(dto))
        {
            // 插入日志
            _operLog.AddOperLog("更新", $"更新照片: {dto.Id}");
            return Ok("更新成功");
        }

        return Problem("更新失败");
    }

    [HttpDelete]
    public async Task<IActionResult> Delete([FromBody] object[] ids)
    {
        // 删除
        if (await _picture.DeleteRange(ids))
        {
            // 插入日志
            _operLog.AddOperLog("删除", $"删除照片: {string.Join(',', ids)}");
            return Ok("删除成功");
        }

        return Problem("删除失败");
    }
}