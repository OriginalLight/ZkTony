using Exposure.Api.Models;
using Exposure.Api.Models.Dto;
using SqlSugar;

namespace Exposure.Api.Contracts.Services;

public interface IErrorLogService : IBaseService<ErrorLog>
{
    /// <summary>
    ///     创建崩溃日志
    /// </summary>
    /// <param name="ex"></param>
    void AddErrorLog(Exception ex);

    /// <summary>
    ///     分页获取崩溃日志
    /// </summary>
    /// <param name="dto"></param>
    /// <param name="total"></param>
    /// <returns></returns>
    Task<List<ErrorLog>> GetByPage(ErrorLogQueryDto dto, RefAsync<int> total);

    /// <summary>
    ///     根据id数组获取
    /// </summary>
    /// <param name="ids"></param>
    /// <returns></returns>
    Task<List<ErrorLog>> GetByIds(int[] ids);
    
    /// <summary>
    ///     自动清理
    /// </summary>
    /// <returns></returns>
    Task AutoClear();
}