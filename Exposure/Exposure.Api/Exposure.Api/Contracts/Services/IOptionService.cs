using Exposure.Api.Models;

namespace Exposure.Api.Contracts.Services;

public interface IOptionService : IBaseService<Option>
{
    /// <summary>
    ///     获取Key对应的值
    /// </summary>
    /// <param name="key"></param>
    /// <returns></returns>
    Task<string?> GetOptionValueAsync(string key);

    /// <summary>
    ///     设置Key对应的值
    /// </summary>
    /// <param name="key"></param>
    /// <param name="value"></param>
    /// <returns></returns>
    Task<bool> SetOptionValueAsync(string key, string value);

    /// <summary>
    ///     获取Key对应的值
    /// </summary>
    /// <param name="key"></param>
    /// <returns></returns>
    string? GetOptionValue(string key);

    /// <summary>
    ///     设置Key对应的值
    /// </summary>
    /// <param name="key"></param>
    /// <param name="value"></param>
    /// <returns></returns>
    bool SetOptionValue(string key, string value);
}