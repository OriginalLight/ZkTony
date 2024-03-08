using Exposure.Api.Contracts.Services;
using Exposure.Api.Contracts.SqlSugar;
using Exposure.Api.Models;

namespace Exposure.Api.Services;

public class ApplicationHostService : IHostedService
{
    private readonly IAutoCleanService _autoCleanService;
    private readonly ICameraService _cameraService;
    private readonly IDbContext _dbContext;
    private readonly ISerialPortService _serialPortService;
    private readonly IUsbService _usbService;
    private readonly IUserService _userService;
    private bool _isInitialized;

    #region 构造函数

    public ApplicationHostService(
        IDbContext dbContext,
        IUserService userService,
        IUsbService usbService,
        ICameraService cameraService,
        IAutoCleanService autoCleanService,
        ISerialPortService serialPortService
    )
    {
        _dbContext = dbContext;
        _usbService = usbService;
        _userService = userService;
        _cameraService = cameraService;
        _autoCleanService = autoCleanService;
        _serialPortService = serialPortService;
    }

    #endregion

    #region 初始化

    public async Task StartAsync(CancellationToken cancellationToken)
    {
        if (!_isInitialized)
        {
            _dbContext.CreateTable(false, 50, typeof(User), typeof(Picture), typeof(OperLog), typeof(ErrorLog));
            _serialPortService.Init();
            await _userService.InitializeAsync();
            await _autoCleanService.CleanPreviewAsync();
            await _usbService.InitializeAsync();
        }

        _isInitialized = true;
    }

    #endregion

    #region 停止

    public async Task StopAsync(CancellationToken cancellationToken)
    {
        _cameraService.Stop();
        await Task.CompletedTask;
    }

    #endregion
}