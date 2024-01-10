using Exposure.Api.Contracts.Repositories;
using Exposure.Api.Contracts.Services;
using Exposure.Api.Models;

namespace Exposure.Api.Services;

public class UserService : BaseService<User>, IUserService
{
    private readonly IUserRepository dal;

    // 登录状态
    private User? _logged;

    public UserService(IUserRepository repository) : base(repository)
    {
        dal = repository;
    }

    /// <summary>
    ///     返回当前登录的用户
    /// </summary>
    /// <returns></returns>
    public User? GetLogged()
    {
        // 返回登录状态
        return _logged;
    }

    /// <summary>
    ///     登录
    /// </summary>
    /// <param name="name"></param>
    /// <param name="password"></param>
    /// <returns></returns>
    public async Task<int> LogIn(string name, string password)
    {
        // 查询用户
        var list = await dal.getByWhere(it => it.Name == name);
        // 检查用户是否存在
        if (list.Count == 0) return 1;
        // 获取用户
        var user = list[0];
        // 检查密码
        if (!BCrypt.Net.BCrypt.Verify(password, user.Sha)) return 2;
        // 检查是否被禁用
        if (!user.Enabled) return 3;
        // 检查是否过期
        if (user.Expire < DateTime.Now) return 4;
        // 存储登录状态
        _logged = user;
        // 更新登录时间
        user.LastLoginTime = DateTime.Now;
        await dal.Update(user);
        // 返回成功
        return 0;
    }

    /// <summary>
    ///     注销
    /// </summary>
    public void LogOut()
    {
        // 清除登录状态
        _logged = null;
    }
}