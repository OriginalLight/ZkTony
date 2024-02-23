using Exposure.Api.Contracts.Services;
using Exposure.Api.Contracts.SqlSugar;
using Exposure.Api.Models;

namespace Exposure.Api.Services;

public class ApplicationHostService : IHostedService
{
    
    private readonly IAutoCleanService _autoCleanService;
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
        IAutoCleanService autoCleanService,
        ISerialPortService serialPortService
    )
    {
        _dbContext = dbContext;
        _usbService = usbService;
        _userService = userService;
        _autoCleanService = autoCleanService;
        _serialPortService = serialPortService;
    }

    #endregion

    #region 初始化
    
    public async Task StartAsync(CancellationToken cancellationToken)
    {
        if (!_isInitialized)
        {
            _serialPortService.Init();
            _dbContext.CreateTable(false, 50, typeof(User), typeof(Picture), typeof(OperLog), typeof(ErrorLog));
            await _userService.InitializeAsync();
            await _autoCleanService.CleanPreviewAsync();
            await _usbService.InitializeAsync();
        }

        _isInitialized = true;
        await Task.CompletedTask;
    }

    #endregion

    #region 停止
    
    public async Task StopAsync(CancellationToken cancellationToken)
    {
        await Task.CompletedTask;
    }

    #endregion
}