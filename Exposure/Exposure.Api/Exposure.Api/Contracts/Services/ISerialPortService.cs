namespace Exposure.Api.Contracts.Services;

public interface ISerialPortService
{
    /// <summary>
    ///     获取所有的串口
    /// </summary>
    /// <returns></returns>
    string[] GetPorts();

    /// <summary>
    ///     打开串口
    /// </summary>
    /// <param name="portName"></param>
    /// <param name="baudRate"></param>
    /// <returns></returns>
    bool OpenPort(string portName, int baudRate);

    /// <summary>
    ///     关闭串口
    /// </summary>
    /// <param name="portName"></param>
    void ClosePort(string portName);

    /// <summary>
    ///     写入串口
    /// </summary>
    /// <param name="portName"></param>
    /// <param name="bytes"></param>
    void WritePort(string portName, byte[] bytes);

    /// <summary>
    ///     读取串口
    /// </summary>
    /// <param name="portName"></param>
    /// <returns></returns>
    string? ReadPort(string portName);
}