using Exposure.Api.Contracts.Repositories;
using Exposure.Api.Contracts.SqlSugar;
using Exposure.Api.Models;

namespace Exposure.Api.Repositories;

public class PictureRepository: BaseRepository<Picture>, IPictureRepository
{
    private readonly IDbContext context;
    
    public PictureRepository(IDbContext dbContext) : base(dbContext)
    {
        context = dbContext;
    }
}