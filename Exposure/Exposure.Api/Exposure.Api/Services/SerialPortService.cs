using System.IO.Ports;
using Exposure.Api.Contracts.Services;

namespace Exposure.Api.Services;

public class SerialPortService : ISerialPortService
{
    private readonly IConfiguration _config;
    private readonly IErrorLogService _errorLog;
    private readonly ILogger<SerialPortService> _logger;
    private readonly Dictionary<string, SerialPort> _serialPorts = new();
    
    #region 构造函数

    public SerialPortService(ILogger<SerialPortService> logger, IConfiguration config, IErrorLogService errorLog)
    {
        _config = config;
        _logger = logger;
        _errorLog = errorLog;
    }

    #endregion

    #region 初始化
    
    public void Init()
    {
        // 读取配置文件
        var com1 = _config["SerialPort:Com1"];
        var com2 = _config["SerialPort:Com2"];
        // 打开串口
        if (com1 != null) OpenPort(com1, 115200, "Com1");
        if (com2 != null) OpenPort(com2, 115200, "Com2");
    }

    #endregion

    #region 获取所有可用串口
    
    public string[] GetPorts()
    {
        return _serialPorts.Keys.ToArray();
    }

    #endregion
    
    #region 获取串口
    
    public SerialPort? GetSerialPort(string alias)
    {
        return !_serialPorts.TryGetValue(alias, out var serialPort) ? null : serialPort;
    }
    
    #endregion

    #region 打开串口
    
    public bool OpenPort(string portName, int baudRate, string alias)
    {
        if (_serialPorts.ContainsKey(portName)) return false;
        SerialPort serialPort = new(portName, baudRate);
        try
        {
            serialPort.Open();
            _serialPorts.Add(alias, serialPort);
            _logger.LogInformation("成功打开串口: " + portName + " 波特率: " + baudRate);
            return true;
        }
        catch (Exception e)
        {
            _logger.LogError("打开串口失败：" + portName, e);
            _errorLog.AddErrorLog(e);
        }

        return false;
    }

    #endregion

    #region 关闭串口
    
    public void ClosePort(string alias)
    {
        try
        {
            _serialPorts[alias].Close();
            _serialPorts.Remove(alias);
            _logger.LogInformation("成功关闭串口: " + alias);
        }
        catch (Exception e)
        {
            _logger.LogError("关闭串口失败：" + alias, e);
            _errorLog.AddErrorLog(e);
        }
    }

    #endregion

    #region 写入串口
    
    public void WritePort(string alias, byte[] bytes)
    {
        try
        {
            _serialPorts[alias].Write(bytes, 0, bytes.Length);
            _logger.LogInformation("向串口 " + alias + " 写入数据: " + BitConverter.ToString(bytes));
        }
        catch (Exception e)
        {
            _logger.LogError("写入数据失败" + alias, e);
            _errorLog.AddErrorLog(e);
        }
    }

    #endregion

    #region 读取串口
    
    public string? ReadPort(string alias)
    {
        if (!_serialPorts.TryGetValue(alias, out var value)) return null;
        try
        {
            return value.ReadLine();
        }
        catch (Exception e)
        {
            _logger.LogError("读取串口失败：" + alias, e);
            _errorLog.AddErrorLog(e);
        }

        return null;
    }

    #endregion
}