using Exposure.Api.Contracts.Repositories;
using Exposure.Api.Contracts.Services;
using Exposure.Api.Models;

namespace Exposure.Api.Services;

public class ErrorLogService : BaseService<ErrorLog>, IErrorLogService
{
    private readonly IErrorLogRepository dal;

    public ErrorLogService(IErrorLogRepository repository) : base(repository)
    {
        dal = repository;
    }

    /// <summary>
    ///     创建崩溃日志
    /// </summary>
    /// <param name="ex"></param>
    public void Create(Exception ex)
    {
        dal.AddReturnIdentity(new ErrorLog
        {
            Message = ex.Message,
            StackTrace = ex.StackTrace,
            Source = ex.Source,
            Type = ex.GetType().ToString(),
            Time = DateTime.Now
        });
    }
}