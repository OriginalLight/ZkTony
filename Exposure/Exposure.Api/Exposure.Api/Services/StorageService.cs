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
            //删除预览文件夹里面的所有文件
            try
            {
                Directory.Delete(FileUtils.Preview, true);
                Log.Information("清空预览图成功");
            }
            catch (Exception e)
            {
                errorLog.AddErrorLog(e);
                Log.Error(e, "清空预览图失败");
            }
            
            // 删除校准文件夹里面的所有文件
            try
            {
                Directory.Delete(FileUtils.Calibration, true);
                Log.Information("清空校准图成功");
            }
            catch (Exception e)
            {
                errorLog.AddErrorLog(e);
                Log.Error(e, "清空校准图失败");
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