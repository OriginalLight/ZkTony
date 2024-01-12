using Exposure.Api.Contracts.Services;
using Exposure.Api.Contracts.SqlSugar;
using Exposure.Api.Models;

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
}