using Exposure.Api.Models;

namespace Exposure.Api.Contracts.Services;

public interface IOperLogService : IBaseService<OperLog>
{
    /// <summary>
    ///     创建操作日志
    /// </summary>
    /// <param name="type"></param>
    /// <param name="desc"></param>
    void Create(string type, string desc);
}