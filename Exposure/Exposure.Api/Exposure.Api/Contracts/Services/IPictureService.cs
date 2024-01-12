using Exposure.Api.Models;
using Exposure.Api.Models.Dto;
using SqlSugar;

namespace Exposure.Api.Contracts.Services;

public interface IPictureService : IBaseService<Picture>
{
    /// <summary>
    ///     分页查询
    /// </summary>
    /// <param name="dto"></param>
    /// <returns></returns>
    Task<List<Picture>> GetByPage(PictureQueryDto dto, RefAsync<int> total);
}