using System.Runtime.InteropServices;
using Exposure.Contracts.Services;
using Exposure.Helpers;
using Logging;

namespace Exposure.Services;

public class VisionService : IVisionService
{
    //存储用来判断是否已经格式化
    private bool isInit = false;

    public async Task InitAsync()
    {
        if (isInit)
        {
            return;
        }

        VisionHelper.Init();
        GlobalLog.Logger?.ReportInfo("初始化SDK");

        isInit = true;

        var device = SearchforDevice();
        if (device == 0)
        {
            GlobalLog.Logger?.ReportInfo("未找到设备");
        }
        else
        {
            GlobalLog.Logger?.ReportInfo($"找到设备数量：{device}");
        }

        await Task.CompletedTask;
    }

    public async Task UninitAsync()
    {
        if (!isInit)
        {
            return;
        }

        VisionHelper.Uninit();
        GlobalLog.Logger?.ReportInfo("释放SDK");

        isInit = false;

        await Task.CompletedTask;
    }

    public string GetVisionText()
    {
        return Marshal.PtrToStringAnsi(VisionHelper.GetVersionText()) ?? "Unknow";
    }

    public int SearchforDevice()
    {
        return VisionHelper.SearchforDevice();
    }
}