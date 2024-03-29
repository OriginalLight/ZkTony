using Exposure.Api.Contracts.Services;
using Exposure.Api.Utils;

namespace Exposure.Api.Services;

public class StorageService(
    ILogger<StorageService> logger,
    IErrorLogService errorLog
) : IStorageService
{
    #region 清理预览图

    public async Task CleanPreviewAsync()
    {
        await Task.Run(() =>
        {
            //删除文件夹里面的所有文件
            try
            {
                if (!Directory.Exists(FileUtils.Preview))
                {
                    return;
                }
                foreach (var file in Directory.GetFiles(FileUtils.Preview))
                {
                    logger.LogInformation($"删除文件：{file}");
                    File.Delete(file);
                }
            }
            catch (Exception e)
            {
                errorLog.AddErrorLog(e);
                logger.LogError(e, "清理预览图失败");
            }
        });
    }

    #endregion
    
    #region 存储空间检测
    
    public double AvailableStorage()
    {
        var drives = DriveInfo.GetDrives();
        var total = drives.Sum(drive => drive.TotalSize);
        var available = drives.Sum(drive => drive.AvailableFreeSpace);
        return available * 1.0 / total;
    }
    
    #endregion
}