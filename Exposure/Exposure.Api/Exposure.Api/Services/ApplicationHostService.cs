using Exposure.Api.Contracts.Services;
using Exposure.Api.Contracts.SqlSugar;
using Exposure.Api.Models;

namespace Exposure.Api.Services;

public class ApplicationHostService(
    IDbContext dbContext,
    IUsbService usbService,
    IUserService userService,
    ICameraService cameraService,
    IAutoCleanService autoCleanService,
    ISerialPortService serialPortService
) : IHostedService
{
    private bool _isInitialized;

    #region 初始化

    public async Task StartAsync(CancellationToken cancellationToken)
    {
        if (!_isInitialized)
        {
            dbContext.CreateTable(false, 50, typeof(User), typeof(Picture), typeof(OperLog), typeof(ErrorLog));
            serialPortService.Init();
            await userService.InitializeAsync();
            await autoCleanService.CleanPreviewAsync();
            await usbService.InitializeAsync();
        }

        _isInitialized = true;
    }

    #endregion

    #region 停止

    public async Task StopAsync(CancellationToken cancellationToken)
    {
        cameraService.Stop();
        await Task.CompletedTask;
    }

    #endregion
}