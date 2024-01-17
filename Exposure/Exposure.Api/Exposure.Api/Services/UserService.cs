using Exposure.Api.Contracts.Services;
using Exposure.Api.Contracts.SqlSugar;
using Exposure.Api.Models;
using Exposure.Api.Models.Dto;
using SqlSugar;

namespace Exposure.Api.Services;

public class UserService : BaseService<User>, IUserService
{
    private readonly IDbContext context;

    // 登录状态
    private User? _logged;

    public UserService(IDbContext dbContext) : base(dbContext)
    {
        context = dbContext;
    }

    /// <summary>
    ///     返回当前登录的用户
    /// </summary>
    /// <returns></returns>
    public User? GetLogged()
    {
        // 返回登录状态
        var user = _logged;
        if (user != null) user.Sha = "";
        return user;
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
        var list = await context.db.Queryable<User>().Where(u => u.Name == name).ToListAsync();
        // 检查用户是否存在
        if (list.Count == 0) return 1;
        // 获取用户
        var user = list[0];
        // 检查密码
        if (!BCrypt.Net.BCrypt.Verify(password, user.Sha)) return 2;
        // 检查是否被禁用
        if (!user.Enabled) return 3;
        // 存储登录状态
        _logged = user;
        // 更新登录时间
        user.LastLoginTime = DateTime.Now;
        await context.db.Updateable(user).ExecuteCommandAsync();
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

    /// <summary>
    ///     分页查询
    /// </summary>
    /// <param name="dto"></param>
    /// <param name="total"></param>
    /// <returns></returns>
    public async Task<List<User>> GetByPage(UserQueryDto dto, RefAsync<int> total)
    {
        return await context.db.Queryable<User>()
            .Select(p => new User
            {
                Id = p.Id,
                Name = p.Name,
                Role = p.Role,
                Enabled = p.Enabled,
                CreateTime = p.CreateTime,
                UpdateTime = p.UpdateTime,
                LastLoginTime = p.LastLoginTime
            })
            .WhereIF(!string.IsNullOrEmpty(dto.Name), p => p.Name.Contains(dto.Name!))
            .WhereIF(_logged != null, p => p.Role > _logged!.Role)
            .OrderBy(p => p.CreateTime, OrderByType.Desc)
            .ToPageListAsync(dto.Page, dto.Size, total);
    }

    /// <summary>
    ///  根据名称查询
    /// </summary>
    /// <param name="dtoName"></param>
    /// <returns></returns>
    public async Task<User?> GetByName(string dtoName)
    {
        return await context.db.Queryable<User>().Where(u => u.Name == dtoName).FirstAsync();
    }
}