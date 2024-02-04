using Exposure.Api.Contracts.Services;

namespace Exposure.Api.Services;

public class AutoCleanService : IAutoCleanService
{
    private readonly IErrorLogService _errorLog;

    #region 构造函数

    public AutoCleanService(IErrorLogService errorLog)
    {
        _errorLog = errorLog;
    }

    #endregion

    #region 清理预览图

    /// <summary>
    ///     清理预览图
    /// </summary>
    /// <returns></returns>
    public Task CleanPreviewAsync()
    {
        var path = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.MyPictures), "Preview");
        if (!Directory.Exists(path)) return Task.CompletedTask;
        //删除文件夹
        try
        {
            Directory.Delete(path, true);
        }
        catch (Exception e)
        {
            _errorLog.AddErrorLog(e);
        }

        return Task.CompletedTask;
    }

    #endregion
}