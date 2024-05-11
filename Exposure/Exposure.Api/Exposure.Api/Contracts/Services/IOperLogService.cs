using Exposure.Api.Models;
using Exposure.Api.Models.Dto;
using SqlSugar;

namespace Exposure.Api.Contracts.Services;

public interface IOperLogService : IBaseService<OperLog>
{
    /// <summary>
    ///     创建操作日志
    /// </summary>
    /// <param name="type"></param>
    /// <param name="desc"></param>
    void AddOperLog(string type, string desc);

    /// <summary>
    ///     分页查询操作日志
    /// </summary>
    /// <param name="dto"></param>
    /// <param name="total"></param>
    /// <returns></returns>
    Task<List<OperLogOutDto>> GetByPage(OperLogQueryDto dto, RefAsync<int> total);

    /// <summary>
    ///     根据id查询
    /// </summary>
    /// <param name="ids"></param>
    /// <returns></returns>
    Task<List<OperLogOutDto>> GetByIds(object[] ids);
    
    /// <summary>
    ///     自动清理
    /// </summary>
    /// <returns></returns>
    Task AutoClear();
}