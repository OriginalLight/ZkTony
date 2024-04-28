using System.IO.Ports;
using System.Text;
using Exposure.Api.Contracts.Services;
using Exposure.Protocal.Default;
using Serilog;

namespace Exposure.Api.Services;

public class SerialPortService(IOptionService option, IErrorLogService errorLog)
    : ISerialPortService
{
    private readonly Dictionary<string, int> _flags = new();
    private readonly Dictionary<string, SerialPort> _serialPorts = new();
    private string _ver = "1.0.0";

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
            try
            {
                var sp = (SerialPort)sender;
                var bytes = new byte[sp.BytesToRead];
                sp.Read(bytes, 0, bytes.Length);
                Log.Information("Com2 收到: " + BitConverter.ToString(bytes));
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
                            Log.Error(e, e.Message);
                        }
                    }
                        break;
                    case 0xFE:
                    {
                        try
                        {
                            var arr = bytes.Skip(5).Take(5).ToArray();
                            // arr to string
                            var str = Encoding.ASCII.GetString(arr);
                            Log.Information("下位机版本：" + str);
                            _ver = str;
                        }
                        catch (Exception e)
                        {
                            Log.Error(e, e.Message);
                        }
                    }
                        break;
                }
            }
            catch (Exception e)
            {
                errorLog.AddErrorLog(e);
                Log.Error(e, e.Message);
            }
        };

        // 设置LED灯
        SetFlag("led", 1);

        // 查询门的状态
        WritePort("Com2", DefaultProtocol.QueryOptocoupler().ToBytes());

        await Task.Delay(100);
        // 查询版本号
        WritePort("Com2", DefaultProtocol.QueryVer().ToBytes());
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
            Log.Information(alias + " 发送: " + BitConverter.ToString(bytes));
        }
        catch (Exception e)
        {
            Log.Error(e, "发送失败" + alias);
            errorLog.AddErrorLog(e);
        }
    }

    #endregion

    #region 获取版本号

    public string GetVer()
    {
        return _ver;
    }

    #endregion

    #region 打开串口

    private async Task OpenPort(string alias)
    {
        if (_serialPorts.ContainsKey(alias))
        {
            Log.Information("已经打开串口: " + alias);
            return;
        }
        var portName = await option.GetOptionValueAsync(alias);
        if (portName == null)
        {
            Log.Information("找不到串口号: " + alias);
            return;
        }
        if (_serialPorts.Any(kv => kv.Value.PortName == portName))
        {
            Log.Information("已经打开串口: " + portName);
            return;
        }
        
        SerialPort serialPort = new(portName, 115200, Parity.None, 8, StopBits.One);
        try
        {
            serialPort.Open();
            _serialPorts.Add(alias, serialPort);
            Log.Information("打开串口成功: " + portName);
        }
        catch (Exception e)
        {
            Log.Error(e, "打开串口失败：" + portName);
            errorLog.AddErrorLog(e);
        }
    }

    #endregion
}