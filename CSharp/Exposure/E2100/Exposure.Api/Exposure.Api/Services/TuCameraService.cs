using System.Runtime.InteropServices;
using Exposure.Api.Contracts.Services;
using Exposure.Api.Models;
using Exposure.Api.Models.Dto;
using TUCAMERA;

namespace Exposure.Api.Services;

public class TuCameraService : ICameraService
{
    TUCAM_INIT _mItApi; // 初始化SDK环境
    TUCAM_OPEN _mOpCam; // 打开相机参数
    
    public Nncam? Camera { get; }
    public Task InitAsync()
    {
        var strPath = Marshal.StringToHGlobalAnsi(Environment.CurrentDirectory);
        _mItApi.uiCamCount = 0;
        _mItApi.pstrConfigPath = strPath;
        TUCamera.TUCAM_Api_Init(ref _mItApi);
        
        if (0 == _mItApi.uiCamCount)
        {
            throw new Exception("No camera found");
        }
        
        _mOpCam.uiIdxOpen = 0;
        TUCamera.TUCAM_Dev_Open(ref _mOpCam);
        // 关闭自动曝光
        TUCamera.TUCAM_Capa_SetValue(_mOpCam.hIdxTUCam, (int)TUCAM_IDCAPA.TUIDC_ATEXPOSURE, 0);
        // 关闭自动白平衡
        TUCamera.TUCAM_Capa_SetValue(_mOpCam.hIdxTUCam, (int)TUCAM_IDCAPA.TUIDC_ATWBALANCE, 0);
        // 设置分辨率
        TUCamera.TUCAM_Capa_SetValue(_mOpCam.hIdxTUCam, (int)TUCAM_IDCAPA.TUIDC_RESOLUTION, 0);
        // 设置bin 0: 1x1 1: 2x2 2: 3x3 3: 4x4 4: 6x6
        TUCamera.TUCAM_Capa_SetValue(_mOpCam.hIdxTUCam, (int)TUCAM_IDCAPA.TUIDC_BINNING_SUM, 4);
        // 设置增益值
        TUCamera.TUCAM_Prop_SetValue(_mOpCam.hIdxTUCam, (int)TUCAM_IDPROP.TUIDP_GLOBALGAIN, 3, 0);
        
        return Task.CompletedTask;
    }
    public void Stop()
    {
        throw new NotImplementedException();
    }
    public Task SetPixel(uint index)
    {
        throw new NotImplementedException();
    }
    public double GetTemperature()
    {
        double lvalue = 1.0f;
        TUCamera.TUCAM_Prop_GetValue(_mOpCam.hIdxTUCam, (int)TUCAM_IDPROP.TUIDP_TEMPERATURE, ref lvalue, 0);
        return lvalue;
    }
    public Task<Photo> PreviewAsync()
    {
        throw new NotImplementedException();
    }
    public Task<long> TakeAutoPhotoAsync(CancellationToken ctsToken)
    {
        throw new NotImplementedException();
    }
    public Task TakeManualPhotoAsync(uint exposure, int frame, CancellationToken ctsToken)
    {
        throw new NotImplementedException();
    }
    public Task CancelTask()
    {
        throw new NotImplementedException();
    }
    public Task<AlbumOutDto?> GetCacheAsync()
    {
        throw new NotImplementedException();
    }
    public Task AgingTest()
    {
        throw new NotImplementedException();
    }
    public Task Collect(int start, int interval, int number)
    {
        throw new NotImplementedException();
    }
    public Task LostTest(int number)
    {
        throw new NotImplementedException();
    }
    public Task Calibrate()
    {
        throw new NotImplementedException();
    }
    public Task ClearAlbum()
    {
        throw new NotImplementedException();
    }
}