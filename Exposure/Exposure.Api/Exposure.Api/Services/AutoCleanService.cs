using Exposure.Api.Contracts.Services;
using Exposure.Api.Utils;

namespace Exposure.Api.Services;

public class AutoCleanService(
    ILogger<AutoCleanService> logger,
    IErrorLogService errorLog
) : IAutoCleanService
{
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
                    logger.LogInformation($"删除文件：{file}");
                    File.Delete(file);
                }
            }
            catch (Exception e)
            {
                errorLog.AddErrorLog(e);
            }
        });
    }

    #endregion
}