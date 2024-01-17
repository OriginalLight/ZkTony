using Exposure.Api.Contracts.Services;
using Exposure.Api.Contracts.SqlSugar;
using Exposure.Api.Models;
using Exposure.Api.Models.Dto;
using SqlSugar;

namespace Exposure.Api.Services;

public class ErrorLogService : BaseService<ErrorLog>, IErrorLogService
{
    private readonly IDbContext context;

    public ErrorLogService(IDbContext dbContext) : base(dbContext)
    {
        context = dbContext;
    }

    /// <summary>
    ///     创建崩溃日志
    /// </summary>
    /// <param name="ex"></param>
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
        context.db.Insertable(errLog).ExecuteReturnIdentity();
    }

    public async Task<List<ErrorLog>> GetByPage(ErrorLogQueryDto dto, RefAsync<int> total)
    {
        return await context.db.Queryable<ErrorLog>()
            .WhereIF(dto.Date != null, p => p.Time >= dto.Date)
            .WhereIF(dto.Date != null, p => p.Time < dto.Date.Value.AddDays(1))
            .OrderBy(p => p.Time, OrderByType.Desc)
            .ToPageListAsync(dto.Page, dto.Size, total);
    }

    public async Task<List<ErrorLog>> GetByIds(object[] ids)
    {
        return await context.db.Queryable<ErrorLog>().Where(p => ids.Contains(p.Id)).ToListAsync();
    }
}