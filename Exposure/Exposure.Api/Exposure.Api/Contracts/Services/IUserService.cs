using Exposure.Api.Models;

namespace Exposure.Api.Contracts.Services;

public interface IUserService : IBaseService<User>
{
    /// <summary>
    ///     返回当前登录的用户
    /// </summary>
    /// <returns></returns>
    User? GetLogged();

    /// <summary>
    ///     登录
    /// </summary>
    /// <param name="name"></param>
    /// <param name="password"></param>
    /// <returns></returns>
    Task<int> LogIn(string name, string password);

    /// <summary>
    ///     注销
    /// </summary>
    void LogOut();
}