using Exposure.Api.Models;
using Exposure.Api.Models.Dto;
using SqlSugar;

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

    /// <summary>
    ///     分页查询
    /// </summary>
    /// <param name="dto"></param>
    /// <returns></returns>
    Task<List<User>> GetByPage(UserQueryDto dto, RefAsync<int> total);
    /// <summary>
    ///   根据名称查询
    /// </summary>
    /// <param name="dtoName"></param>
    /// <returns></returns>
    Task<User?> GetByName(string dtoName);
}