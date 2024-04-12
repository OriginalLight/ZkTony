using Exposure.Api.Contracts.Services;
using Exposure.Utilities;
using Serilog;

namespace Exposure.Api.Services;

public class StorageService(
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
                if (!Directory.Exists(FileUtils.Preview)) return;
                foreach (var file in Directory.GetFiles(FileUtils.Preview))
                {
                    Log.Information($"删除文件：{file}");
                    File.Delete(file);
                }
            }
            catch (Exception e)
            {
                errorLog.AddErrorLog(e);
                Log.Error(e, "清空预览图失败");
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
        Log.Information($"Available Storage：{available} / {total}");
        return available * 1.0 / total;
    }

    #endregion
}