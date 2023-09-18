using System.Runtime.InteropServices;
using Exposure.Contracts.Services;
using Exposure.Helpers;

namespace Exposure.Services;

public class VisionService : IVisionService
{
    public async Task InitAsync()
    {
        VisionHelper.Init();
        await Task.CompletedTask;
    }

    public string? GetVisionText()
    {
        return Marshal.PtrToStringAnsi(VisionHelper.GetVersionText());
    }
}