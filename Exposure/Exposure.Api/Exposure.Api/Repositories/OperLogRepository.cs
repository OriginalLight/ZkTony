using Exposure.Api.Contracts.Repositories;
using Exposure.Api.Contracts.SqlSugar;
using Exposure.Api.Models;

namespace Exposure.Api.Repositories;

public class OperLogRepository : BaseRepository<OperLog>, IOperLogRepository
{
    private readonly IDbContext context;

    public OperLogRepository(IDbContext dbContext) : base(dbContext)
    {
        context = dbContext;
    }
}