using Exposure.Api.Contracts.Services;
using Exposure.Utilities;
using Serilog;

namespace Exposure.Api.Services;

public class StorageService(
    IErrorLogService errorLog,
    IOperLogService operLog
) : IStorageService
{
    #region 清理预览图

    public async Task ClearStorageAsync()
    {
        await Task.Run(async () =>
        {
            //删除文件夹里面的所有文件
            try
            {
                var directory = FileUtils.Preview;
                if (Directory.Exists(directory))
                {
                    foreach (var file in Directory.GetFiles(directory))
                    {
                        Log.Information($"删除文件：{file}");
                        File.Delete(file);
                    }
                }
            }
            catch (Exception e)
            {
                errorLog.AddErrorLog(e);
                Log.Error(e, "清空预览图失败");
            }
            
            // 自动清理错误日志
            await errorLog.AutoClear();
            Log.Information("自动清理错误日志");
            // 自动清理操作日志
            await operLog.AutoClear();
            Log.Information("自动清理操作日志");
            
        });
    }

    #endregion

    #region 存储空间检测

    public double AvailableStorage()
    {
        var drives = DriveInfo.GetDrives();
        var total = drives.Sum(drive => drive.TotalSize);
        var available = drives.Sum(drive => drive.AvailableFreeSpace);
        Log.Information($"Available Storage：{available} / {total}");
        return available * 1.0 / total;
    }

    #endregion
}