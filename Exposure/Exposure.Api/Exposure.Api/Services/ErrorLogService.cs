using Exposure.Api.Contracts.Services;
using Exposure.Api.Contracts.SqlSugar;
using Exposure.Api.Models;
using Exposure.Api.Models.Dto;
using SqlSugar;

namespace Exposure.Api.Services;

public class ErrorLogService : BaseService<ErrorLog>, IErrorLogService
{
    private readonly IDbContext _context;


    #region 构造函数

    public ErrorLogService(IDbContext dbContext) : base(dbContext)
    {
        _context = dbContext;
    }

    #endregion

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

    public async Task<List<ErrorLog>> GetByIds(object[] ids)
    {
        return await _context.db.Queryable<ErrorLog>().Where(p => ids.Contains(p.Id)).ToListAsync();
    }

    #endregion
}