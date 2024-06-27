using Exposure.Api.Models;
using Exposure.Api.Models.Dto;

namespace Exposure.Api.Contracts.Services;

public interface IPhotoService : IBaseService<Photo>
{
    /// <summary>
    ///     添加并返回实体
    /// </summary>
    /// <param name="photo"></param>
    /// <returns></returns>
    Task<Photo> AddReturnModel(Photo photo);

    /// <summary>
    ///     根据id查询
    /// </summary>
    /// <param name="ids"></param>
    /// <returns></returns>
    Task<List<Photo>> GetByIds(int[] ids);
    
    /// <summary>
    ///     根据id查询
    /// </summary>
    /// <param name="id"></param>
    /// <returns></returns>
    Task<List<Photo>> GetByAlbumId(int id);
    
    /// <summary>
    ///     删除多条数据
    /// </summary>
    /// <param name="ids"></param>
    /// <returns></returns>
    Task<bool> DeleteByIds(int[] ids);
    
    /// <summary>
    ///     更新
    /// </summary>
    /// <param name="model"></param>
    /// <returns></returns>
    Task<bool> Update(PhotoUpdateDto model);

    /// <summary>
    ///     调整图片
    /// </summary>
    /// <param name="dto"></param>
    /// <returns></returns>
    Task<Photo?> Adjust(PhotoAdjustDto dto);

    /// <summary>
    ///     合成
    /// </summary>
    /// <param name="ids"></param>
    /// <returns></returns>
    Task<Photo?> Combine(int[] ids);
}