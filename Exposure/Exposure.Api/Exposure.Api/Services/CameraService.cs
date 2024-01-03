using Exposure.Api.Contracts.Services;
using Exposure.External;

namespace Exposure.Api.Services;

public class CameraService : ICameraService
{
    private readonly Nncam _nncam;
    private readonly Rccam _rccam;

    public CameraService()
    {
        _nncam = Nncam.Open(null);
        _rccam = new Rccam();
    }
}