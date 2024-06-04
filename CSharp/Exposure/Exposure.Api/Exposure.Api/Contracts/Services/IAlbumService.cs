using Exposure.Api.Models;
using Exposure.Api.Models.Dto;
using SqlSugar;

namespace Exposure.Api.Contracts.Services;

public interface IAlbumService : IBaseService<Album>
{
    /// <summary>
    ///     分页查询
    /// </summary>
    /// <param name="dto"></param>
    /// <param name="total"></param>
    /// <returns></returns>
    Task<List<AlbumOutDto>> GetByPage(AlbumQueryDto dto, RefAsync<int> total);

    /// <summary>
    ///     添加并返回实体
    /// </summary>
    /// <param name="album"></param>
    /// <returns></returns>
    Task<Album> AddReturnModel(Album album);

    /// <summary>
    ///     根据id查询
    /// </summary>
    /// <param name="ids"></param>
    /// <returns></returns>
    Task<List<AlbumOutDto>> GetByIds(int[] ids);
    
    
    /// <summary>
    ///     根据id查询
    /// </summary>
    /// <param name="id"></param>
    /// <returns></returns>
    Task<AlbumOutDto> GetById(int id);
    
    /// <summary>
    ///     删除多条数据
    /// </summary>
    /// <param name="keys"></param>
    /// <returns></returns>
    Task<bool> DeleteByIds(int[] keys);
    
    /// <summary>
    ///     更新
    /// </summary>
    /// <param name="model"></param>
    /// <returns></returns>
    Task<bool> Update(AlbumUpdateDto model);
}