using Exposure.Api.Contracts.Services;
using Exposure.Api.Core;
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
    public async Task<HttpResult> Page([FromBody] PictureQueryDto dto)
    {
        // 查询日志
        _operLog.AddOperLog("查询", $"查询照片: 页码 = {dto.Page}, 大小 = {dto.Size}");
        // 组合查询条件
        var total = new RefAsync<int>();
        var list = await _picture.GetByPage(dto, total);
        return HttpResult.Success("查询成功", new PageOutDto<List<Picture>>
        {
            Page = dto.Page,
            Size = dto.Size,
            Total = total.Value,
            Data = list
        });
    }
    
    [HttpPut]
    public async Task<HttpResult> Update([FromBody] Picture dto)
    {
        // 查询日志
        _operLog.AddOperLog("更新", $"更新照片: {dto.Id}");
        // 更新
        return await _picture.Update(dto)
            ? HttpResult.Success("更新成功")
            : HttpResult.Fail("更新失败");
    }
    
    [HttpDelete]
    public async Task<HttpResult> Delete([FromBody] object[] ids)
    {
        // 查询日志
        _operLog.AddOperLog("删除", $"删除照片: {string.Join(',', ids)}");
        // 删除
        return await _picture.DeleteRange(ids)
            ? HttpResult.Success("删除成功")
            : HttpResult.Fail("删除失败");
    }
}