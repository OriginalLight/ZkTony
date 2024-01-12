using Exposure.Api.Contracts.Services;
using Exposure.Api.Contracts.SqlSugar;
using Exposure.Api.Models;

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
}