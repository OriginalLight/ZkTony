using System.IO.Ports;

namespace Exposure.Api.Contracts.Services;

public interface ISerialPortService
{
    /// <summary>
    ///   初始化
    /// </summary>
    /// <returns></returns>
    void Init();
    /// <summary>
    ///     获取所有的串口
    /// </summary>
    /// <returns></returns>
    string[] GetPorts();
    
    /// <summary>
    ///   获取串口
    /// </summary>
    /// <param name="alias"></param>
    /// <returns></returns>
    SerialPort? GetSerialPort(string alias);

    /// <summary>
    ///     打开串口
    /// </summary>
    /// <param name="portName"></param>
    /// <param name="baudRate"></param>
    /// <param name="alias"></param>
    /// <returns></returns>
    bool OpenPort(string portName, int baudRate, string alias);

    /// <summary>
    ///     关闭串口
    /// </summary>
    /// <param name="alias"></param>
    void ClosePort( string alias);

    /// <summary>
    ///     写入串口
    /// </summary>
    /// <param name="alias"></param>
    /// <param name="bytes"></param>
    void WritePort(string alias, byte[] bytes);

    /// <summary>
    ///     读取串口
    /// </summary>
    /// <param name="alias"></param>
    /// <returns></returns>
    string? ReadPort(string alias);
}