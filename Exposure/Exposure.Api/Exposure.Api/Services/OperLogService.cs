using Exposure.Api.Contracts.Services;
using Exposure.Api.Contracts.SqlSugar;
using Exposure.Api.Models;
using Exposure.Api.Models.Dto;
using SqlSugar;

namespace Exposure.Api.Services;

public class OperLogService : BaseService<OperLog>, IOperLogService
{
    private readonly IUserService _user;
    private readonly IDbContext context;

    public OperLogService(IDbContext dbContext, IUserService userService) : base(dbContext)
    {
        context = dbContext;
        _user = userService;
    }


    public void AddOperLog(string type, string desc)
    {
        var logged = _user.GetLogged();
        if (logged != null)
        {
            var operLog = new OperLog
            {
                UserId = logged.Id,
                Type = type,
                Description = desc,
                Time = DateTime.Now
            };
            context.db.Insertable(operLog).ExecuteReturnIdentity();
        }
    }

    public async Task<List<OperLogOutDto>> GetByPage(OperLogQueryDto dto, RefAsync<int> total)
    {
        var list = await context.db.Queryable<OperLog>()
            .WhereIF(dto.Date != null, p => p.Time >= dto.Date)
            .WhereIF(dto.Date != null, p => p.Time < dto.Date!.Value.AddDays(1))
            .OrderBy(p => p.Time, OrderByType.Desc)
            .ToPageListAsync(dto.Page, dto.Size, total);
        // 提取出userI并去重
        var ids = list.Select(p => p.UserId).Distinct().ToArray();
        // 查询用户
        var users = await context.db.Queryable<User>().Select(p => new User
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

    public async Task<List<OperLogOutDto>> GetByIds(object[] ids)
    {
        var list = await context.db.Queryable<OperLog>().Where(p => ids.Contains(p.Id)).ToListAsync();
        // 提取出userI并去重
        var userIds = list.Select(p => p.UserId).Distinct().ToArray();
        // 查询用户
        var users = await context.db.Queryable<User>().Select(p => new User
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
}