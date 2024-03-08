using Exposure.Api.Contracts.Services;
using Exposure.Api.Utils;

namespace Exposure.Api.Services;

public class AutoCleanService : IAutoCleanService
{
    private readonly IErrorLogService _errorLog;
    private readonly ILogger<AutoCleanService> _logger;

    #region 构造函数

    public AutoCleanService(ILogger<AutoCleanService> logger, IErrorLogService errorLog)
    {
        _logger = logger;
        _errorLog = errorLog;
    }

    #endregion

    #region 清理预览图

    public async Task CleanPreviewAsync()
    {
        await Task.Run(() =>
        {
            //删除文件夹里面的所有文件
            try
            {
                foreach (var file in Directory.GetFiles(FileUtils.Preview))
                {
                    _logger.LogInformation($"删除文件：{file}");
                    File.Delete(file);
                }
            }
            catch (Exception e)
            {
                _errorLog.AddErrorLog(e);
            }
        });
    }

    #endregion
}