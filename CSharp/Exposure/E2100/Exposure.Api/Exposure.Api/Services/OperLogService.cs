using Exposure.Api.Contracts.Services;
using Exposure.Api.Models;
using Exposure.Api.Models.Dto;
using Exposure.SqlSugar.Contracts;
using SqlSugar;

namespace Exposure.Api.Services;

public class OperLogService(IDbContext dbContext, IUserService userService)
    : BaseService<OperLog>(dbContext), IOperLogService
{
    private readonly IDbContext _context = dbContext;


    #region 添加操作日志

    /// <summary>
    ///     添加操作日志
    /// </summary>
    /// <param name="type"></param>
    /// <param name="desc"></param>
    public void AddOperLog(string type, string desc)
    {
        var logged = userService.GetLogged();
        if (logged != null)
        {
            var operLog = new OperLog
            {
                UserId = logged.Id,
                Type = type,
                Description = desc,
                Time = DateTime.Now
            };
            _context.db.Insertable(operLog).ExecuteReturnIdentity();
        }
    }

    #endregion

    #region 分页查询

    /// <summary>
    ///     分页查询
    /// </summary>
    /// <param name="dto"></param>
    /// <param name="total"></param>
    /// <returns></returns>
    public async Task<List<OperLogOutDto>> GetByPage(OperLogQueryDto dto, RefAsync<int> total)
    {
        var list = await _context.db.Queryable<OperLog>()
            .WhereIF(dto.Date != null, p => p.Time >= dto.Date)
            .WhereIF(dto.Date != null, p => p.Time < dto.Date!.Value.AddDays(1))
            .OrderBy(p => p.Time, OrderByType.Desc)
            .ToPageListAsync(dto.Page, dto.Size, total);
        // 提取出userI并去重
        var ids = list.Select(p => p.UserId).Distinct().ToArray();
        // 查询用户
        var users = await _context.db.Queryable<User>().Select(p => new User
        {
            Id = p.Id,
            Name = p.Name,
            Role = p.Role,
            Enabled = p.Enabled,
            CreateTime = p.CreateTime,
            UpdateTime = p.UpdateTime,
            LastLoginTime = p.LastLoginTime
        }).Where(u => ids.Contains(u.Id)).ToListAsync();

        return list.Select(p => new OperLogOutDto
        {
            Id = p.Id,
            User = users.FirstOrDefault(u => u.Id == p.UserId) ?? null,
            Type = p.Type,
            Description = p.Description,
            Time = p.Time
        }).ToList();
    }

    #endregion

    #region 根据id查询

    /// <summary>
    ///     根据id查询
    /// </summary>
    /// <param name="ids"></param>
    /// <returns></returns>
    public async Task<List<OperLogOutDto>> GetByIds(int[] ids)
    {
        var list = await _context.db.Queryable<OperLog>().Where(p => ids.Contains(p.Id)).ToListAsync();
        // 提取出userI并去重
        var userIds = list.Select(p => p.UserId).Distinct().ToArray();
        // 查询用户
        var users = await _context.db.Queryable<User>().Select(p => new User
        {
            Id = p.Id,
            Name = p.Name,
            Role = p.Role,
            Enabled = p.Enabled,
            CreateTime = p.CreateTime,
            UpdateTime = p.UpdateTime,
            LastLoginTime = p.LastLoginTime
        }).Where(u => userIds.Contains(u.Id)).ToListAsync();

        return list.Select(p => new OperLogOutDto
        {
            Id = p.Id,
            User = users.FirstOrDefault(u => u.Id == p.UserId) ?? null,
            Type = p.Type,
            Description = p.Description,
            Time = p.Time
        }).ToList();
    }

    #endregion

    #region 自动清理

    public async Task AutoClear()
    {
        await _context.db.Deleteable<ErrorLog>().Where(p => p.Time < DateTime.Now.AddDays(-180)).ExecuteCommandAsync();
    }

    #endregion
}