using System.IO.Ports;
using Exposure.Api.Contracts.Services;
using Exposure.Api.Core.SerialPort.Default;

namespace Exposure.Api.Services;

public class SerialPortService(ILogger<SerialPortService> logger, IOptionService option, IErrorLogService errorLog)
    : ISerialPortService
{
    private readonly Dictionary<string, int> _flags = new();
    private readonly Dictionary<string, SerialPort> _serialPorts = new();

    #region 初始化

    public async Task InitAsync()
    {
        // 打开串口
        await OpenPort("Com1");
        await OpenPort("Com2");
        // 设置串口接收事件
        if (!_serialPorts.TryGetValue("Com2", out var serialPort)) return;
        serialPort.DataReceived += (sender, _) =>
        {
            var sp = (SerialPort)sender;
            var bytes = new byte[sp.BytesToRead];
            sp.Read(bytes, 0, bytes.Length);
            logger.LogInformation("Com2 接收到数据: " + BitConverter.ToString(bytes));
            switch (bytes[2])
            {
                case 0x03:
                    WritePort("Com2", DefaultProtocol.QueryOptocoupler().ToBytes());
                    break;
                case 0x04:
                {
                    try
                    {
                        SetFlag("hatch", bytes[6] == 0x00 ? 1 : 0);
                    }
                    catch (Exception e)
                    {
                        logger.LogError(e.Message);
                    }
                }
                    break;
            }
        };

        // 设置LED灯
        SetFlag("led", 1);

        // 查询门的状态
        WritePort("Com2", DefaultProtocol.QueryOptocoupler().ToBytes());
    }

    #endregion

    #region 获取标志

    public int GetFlag(string alias)
    {
        return _flags.TryGetValue(alias, out var flag) ? flag : 0;
    }

    #endregion

    #region 设置标志

    public void SetFlag(string alias, int flag)
    {
        _flags[alias] = flag;
    }

    #endregion

    #region 获取所有可用串口

    public string[] GetPorts()
    {
        return SerialPort.GetPortNames();
    }

    #endregion

    #region 写入串口

    public void WritePort(string alias, byte[] bytes)
    {
        try
        {
            _serialPorts[alias].Write(bytes, 0, bytes.Length);
            logger.LogInformation("向串口 " + alias + " 写入数据: " + BitConverter.ToString(bytes));
        }
        catch (Exception e)
        {
            logger.LogError("写入数据失败" + alias, e);
            errorLog.AddErrorLog(e);
        }
    }

    #endregion

    #region 打开串口

    private async Task OpenPort(string alias)
    {
        var portName = await option.GetOptionValueAsync(alias);
        if (portName == null) return;

        if (_serialPorts.ContainsKey(portName)) return;
        SerialPort serialPort = new(portName, 115200, Parity.None, 8, StopBits.One);
        try
        {
            serialPort.Open();
            _serialPorts.Add(alias, serialPort);
            logger.LogInformation("成功打开串口: " + portName);
        }
        catch (Exception e)
        {
            logger.LogError("打开串口失败：" + portName, e);
            errorLog.AddErrorLog(e);
        }
    }

    #endregion
}