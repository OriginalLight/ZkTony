using Exposure.Api.Contracts.Services;
using Exposure.Api.Models;
using Exposure.Api.Models.Dto;
using Exposure.SqlSugar.Contracts;
using SqlSugar;

namespace Exposure.Api.Services;

public class UserService(IDbContext dbContext) : BaseService<User>(dbContext), IUserService
{
    private readonly IDbContext _context = dbContext;

    // 登录状态
    private User? _user;

    #region 初始化

    public async Task InitializeAsync()
    {
        // 查询超级管理员是否存在
        var su = await _context.db.Queryable<User>().Where(it => it.Role == 0).FirstAsync();
        if (su == null)
        {
            su = new User
            {
                Name = "zkty",
                Sha = BCrypt.Net.BCrypt.HashPassword("zkty"),
                Role = 0,
                Enabled = true,
                CreateTime = DateTime.Now,
                UpdateTime = DateTime.Now,
                LastLoginTime = DateTime.Now
            };
            await _context.db.Insertable(su).ExecuteReturnIdentityAsync();
        }
    }

    #endregion

    #region 返回当前登录的用户

    public User? GetLogged()
    {
        // 返回登录状态
        var user = _user;
        if (user != null) user.Sha = "";
        return user;
    }

    #endregion

    #region 登录

    public async Task<int> LogIn(string name, string password)
    {
        // 查询用户
        var list = await _context.db.Queryable<User>().Where(u => u.Name == name).ToListAsync();
        // 检查用户是否存在
        if (list.Count == 0) return 1;
        // 获取用户
        var user = list[0];
        // 检查密码
        if (!BCrypt.Net.BCrypt.Verify(password, user.Sha)) return 2;
        // 检查是否被禁用
        if (!user.Enabled) return 3;
        // 存储登录状态
        _user = user;
        // 更新登录时间
        user.LastLoginTime = DateTime.Now;
        await _context.db.Updateable(user).ExecuteCommandAsync();
        // 返回成功
        return 0;
    }

    #endregion

    #region 注销

    public void LogOut()
    {
        // 清除登录状态
        _user = null;
    }

    #endregion

    #region 分页查询

    public async Task<List<User>> GetByPage(UserQueryDto dto, RefAsync<int> total)
    {
        return await _context.db.Queryable<User>()
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
            .WhereIF(_user != null, p => p.Role > _user!.Role)
            .OrderBy(p => p.CreateTime, OrderByType.Desc)
            .ToPageListAsync(dto.Page, dto.Size, total);
    }

    #endregion

    #region 根据名称查询

    public async Task<User?> GetByName(string dtoName)
    {
        return await _context.db.Queryable<User>().Where(u => u.Name == dtoName).FirstAsync();
    }

    #endregion
}