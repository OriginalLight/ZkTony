namespace Exposure.Api.Contracts.Services;

public interface ISerialPortService
{
    /// <summary>
    ///     初始化
    /// </summary>
    /// <returns></returns>
    Task InitAsync();

    /// <summary>
    ///     获取标志
    /// </summary>
    /// <param name="alias"></param>
    /// <returns></returns>
    int GetFlag(string alias);

    /// <summary>
    ///     设置标志
    /// </summary>
    /// <param name="alias"></param>
    /// <param name="flag"></param>
    void SetFlag(string alias, int flag);

    /// <summary>
    ///     获取所有的串口
    /// </summary>
    /// <returns></returns>
    string[] GetPorts();

    /// <summary>
    ///     写入串口
    /// </summary>
    /// <param name="alias"></param>
    /// <param name="bytes"></param>
    void WritePort(string alias, byte[] bytes);


    /// <summary>
    ///     获取版本号
    /// </summary>
    /// <returns></returns>
    string GetVer();
}