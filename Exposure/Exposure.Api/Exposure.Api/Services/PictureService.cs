using Exposure.Api.Contracts.Repositories;
using Exposure.Api.Contracts.Services;
using Exposure.Api.Models;

namespace Exposure.Api.Services;

public class PictureService : BaseService<Picture> , IPictureService
{
    private readonly IPictureRepository dal;


    public PictureService(IPictureRepository repository) : base(repository)
    {
        dal = repository;
    }
}