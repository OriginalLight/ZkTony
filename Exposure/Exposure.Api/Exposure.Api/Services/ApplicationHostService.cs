using Exposure.Api.Contracts.Services;
using Exposure.Api.Contracts.SqlSugar;
using Exposure.Api.Models;

namespace Exposure.Api.Services;

public class ApplicationHostService : IHostedService
{
    private readonly IDbContext _dbContext;
    private readonly IUsbService _usbService;
    private bool _isInitialized;

    public ApplicationHostService(IDbContext dbContext, IUsbService usbService)
    {
        _dbContext = dbContext;
        _usbService = usbService;
    }

    /// <summary>
    ///     初始化
    /// </summary>
    /// <param name="cancellationToken"></param>
    public async Task StartAsync(CancellationToken cancellationToken)
    {
        if (!_isInitialized)
        {
            _dbContext.CreateTable(false, 50, typeof(User), typeof(Picture), typeof(OperLog), typeof(ErrorLog));
            await _usbService.InitializeAsync();
        }

        _isInitialized = true;
        await Task.CompletedTask;
    }

    /// <summary>
    ///     停止
    /// </summary>
    /// <param name="cancellationToken"></param>
    public async Task StopAsync(CancellationToken cancellationToken)
    {
        await Task.CompletedTask;
    }
}