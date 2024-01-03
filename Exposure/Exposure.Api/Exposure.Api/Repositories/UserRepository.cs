using Exposure.Api.Contracts.Repositories;
using Exposure.Api.Contracts.SqlSugar;
using Exposure.Api.Models;

namespace Exposure.Api.Repositories;

public class UserRepository : BaseRepository<User>, IUserRepository
{
    private readonly IDbContext context;

    public UserRepository(IDbContext dbContext) : base(dbContext)
    {
        context = dbContext;
    }
}