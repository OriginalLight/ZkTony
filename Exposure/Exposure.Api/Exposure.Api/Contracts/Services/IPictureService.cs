﻿using Exposure.Api.Models;
using Exposure.Api.Models.Dto;
using SqlSugar;

namespace Exposure.Api.Contracts.Services;

public interface IPictureService : IBaseService<Picture>
{
    /// <summary>
    ///     分页查询
    /// </summary>
    /// <param name="dto"></param>
    /// <param name="total"></param>
    /// <returns></returns>
    Task<List<Picture>> GetByPage(PictureQueryDto dto, RefAsync<int> total);

    /// <summary>
    ///     添加并返回实体
    /// </summary>
    /// <param name="picture"></param>
    /// <returns></returns>
    Task<Picture> AddReturnModel(Picture picture);

    /// <summary>
    ///     根据id查询
    /// </summary>
    /// <param name="ids"></param>
    /// <returns></returns>
    Task<List<Picture>> GetByIds(object[] ids);
    
    /// <summary>
    ///     删除多条数据
    /// </summary>
    /// <param name="keys"></param>
    /// <returns></returns>
    Task<bool> DeleteByIds(object[] keys);

    /// <summary>
    ///     合成
    /// </summary>
    /// <param name="pic1"></param>
    /// <param name="pic2"></param>
    /// <returns></returns>
    Task<Picture> Combine(Picture pic1, Picture pic2);

    /// <summary>
    ///     调整图片
    /// </summary>
    /// <param name="dto"></param>
    /// <returns></returns>
    Task<Picture> Adjust(PictureAdjustDto dto);

    /// <summary>
    ///     更新
    /// </summary>
    /// <param name="model"></param>
    /// <returns></returns>
    Task<bool> Update(PictureUpdateDto model);
}