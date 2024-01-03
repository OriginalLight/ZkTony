using Exposure.Api.Contracts.Repositories;
using Exposure.Api.Contracts.Services;
using Exposure.Api.Models;

namespace Exposure.Api.Services;

public class OperLogService : BaseService<OperLog>, IOperLogService
{
    private readonly IUserService _user;
    private readonly IOperLogRepository dal;

    public OperLogService(IOperLogRepository repository, IUserService userService) : base(repository)
    {
        dal = repository;
        _user = userService;
    }


    public void Create(string type, string desc)
    {
        var logged = _user.GetLogged();
        if (logged != null)
            dal.AddReturnIdentity(new OperLog
            {
                UserId = logged.Id,
                Type = type,
                Description = desc,
                Time = DateTime.Now
            });
    }
}