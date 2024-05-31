using Exposure.Api.Contracts.Services;
using Exposure.Api.Models;
using Exposure.Api.Models.Dto;
using Exposure.SqlSugar.Contracts;
using SqlSugar;

namespace Exposure.Api.Services;

public class ErrorLogService(IDbContext dbContext) : BaseService<ErrorLog>(dbContext), IErrorLogService
{
    private readonly IDbContext _context = dbContext;


    #region 创建崩溃日志

    public void AddErrorLog(Exception ex)
    {
        var errLog = new ErrorLog
        {
            Message = ex.Message,
            StackTrace = ex.StackTrace,
            Source = ex.Source,
            Type = ex.GetType().ToString(),
            Time = DateTime.Now
        };
        _context.db.Insertable(errLog).ExecuteReturnIdentity();
    }

    #endregion

    #region 分页查询

    public async Task<List<ErrorLog>> GetByPage(ErrorLogQueryDto dto, RefAsync<int> total)
    {
        return await _context.db.Queryable<ErrorLog>()
            .WhereIF(dto.Date != null, p => p.Time >= dto.Date)
            .WhereIF(dto.Date != null, p => dto.Date != null && p.Time < dto.Date.Value.AddDays(1))
            .OrderBy(p => p.Time, OrderByType.Desc)
            .ToPageListAsync(dto.Page, dto.Size, total);
    }

    #endregion

    #region 根据id查询

    public async Task<List<ErrorLog>> GetByIds(int[] ids)
    {
        return await _context.db.Queryable<ErrorLog>().Where(p => ids.Contains(p.Id)).ToListAsync();
    }

    #endregion

    #region 自动清理

    public async Task AutoClear()
    {
        await _context.db.Deleteable<ErrorLog>().Where(p => p.Time < DateTime.Now.AddDays(-30)).ExecuteCommandAsync();
    }

    #endregion
}