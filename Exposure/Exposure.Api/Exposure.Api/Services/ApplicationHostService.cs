using Exposure.Api.Contracts.Services;
using Exposure.Api.Models;
using Exposure.SqlSugar.Contracts;
using Exposure.Utilities;

namespace Exposure.Api.Services;

public class ApplicationHostService(
    IDbContext dbContext,
    IUsbService usbService,
    IUserService userService,
    ICameraService cameraService,
    IStorageService storageService,
    ISerialPortService serialPortService
) : IHostedService
{
    private bool _isInitialized;

    #region 初始化

    public async Task StartAsync(CancellationToken cancellationToken)
    {
        if (!_isInitialized)
        {
            WindowUtils.Hide();
            dbContext.CreateTable(false, 50, typeof(User), typeof(Picture), typeof(OperLog), typeof(ErrorLog),
                typeof(Option));
            await serialPortService.InitAsync();
            await userService.InitializeAsync();
            await storageService.ClearStorageAsync();
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