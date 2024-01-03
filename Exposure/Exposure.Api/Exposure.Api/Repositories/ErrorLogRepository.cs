using Exposure.Api.Contracts.Repositories;
using Exposure.Api.Contracts.SqlSugar;
using Exposure.Api.Models;

namespace Exposure.Api.Repositories;

public class ErrorLogRepository : BaseRepository<ErrorLog>, IErrorLogRepository
{
    private readonly IDbContext context;

    public ErrorLogRepository(IDbContext dbContext) : base(dbContext)
    {
        context = dbContext;
    }
}