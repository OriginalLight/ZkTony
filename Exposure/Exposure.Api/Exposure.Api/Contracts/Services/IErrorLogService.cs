using Exposure.Api.Models;

namespace Exposure.Api.Contracts.Services;

public interface IErrorLogService : IBaseService<ErrorLog>
{
    /// <summary>
    ///     创建崩溃日志
    /// </summary>
    /// <param name="ex"></param>
    void Create(Exception ex);
}