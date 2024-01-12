using System.IO.Ports;
using Exposure.Api.Contracts.Services;

namespace Exposure.Api.Services;

public class SerialPortService : ISerialPortService
{
    private readonly ILogger<SerialPortService> _logger;
    private readonly Dictionary<string, SerialPort> _serialPorts = new();

    public SerialPortService(ILogger<SerialPortService> logger)
    {
        _logger = logger;
    }

    /// <summary>
    ///     获取所有的串口
    /// </summary>
    /// <returns></returns>
    public string[] GetPorts()
    {
        return SerialPort.GetPortNames();
    }

    /// <summary>
    ///     打开串口
    /// </summary>
    /// <param name="portName"></param>
    /// <param name="baudRate"></param>
    /// <returns></returns>
    public bool OpenPort(string portName, int baudRate)
    {
        if (_serialPorts.ContainsKey(portName)) return false;
        SerialPort serialPort = new(portName, baudRate);
        try
        {
            serialPort.Open();
            _serialPorts.Add(portName, serialPort);
            _logger.LogInformation("成功打开串口: " + portName + " 波特率: " + baudRate);
            return true;
        }
        catch (Exception e)
        {
            _logger.LogError("打开串口失败：" + portName, e);
        }

        return false;
    }

    /// <summary>
    ///     关闭串口
    /// </summary>
    /// <param name="portName"></param>
    public void ClosePort(string portName)
    {
        try
        {
            _serialPorts[portName].Close();
            _serialPorts.Remove(portName);
            _logger.LogInformation("成功关闭串口: " + portName);
        }
        catch (Exception e)
        {
            _logger.LogError("关闭串口失败：" + portName, e);
        }
    }

    /// <summary>
    ///     写入串口
    /// </summary>
    /// <param name="portName"></param>
    /// <param name="bytes"></param>
    public void WritePort(string portName, byte[] bytes)
    {
        try
        {
            _serialPorts[portName].Write(bytes, 0, bytes.Length);
            _logger.LogInformation("向串口: " + portName + " 写入数据: " + BitConverter.ToString(bytes));
        }
        catch (Exception e)
        {
            _logger.LogError("写入数据失败" + portName, e);
        }
    }

    /// <summary>
    ///     读取串口
    /// </summary>
    /// <param name="portName"></param>
    /// <returns></returns>
    public string? ReadPort(string portName)
    {
        if (!_serialPorts.TryGetValue(portName, out var value)) return null;
        try
        {
            return value.ReadLine();
        }
        catch (Exception e)
        {
            _logger.LogError("读取串口失败：" + portName, e);
        }

        return null;
    }
}