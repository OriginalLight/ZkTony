﻿using System.IO.Ports;
using System.Text;
using Exposure.Api.Contracts.Services;
using Exposure.Protocal.Default;
using Serilog;

namespace Exposure.Api.Services;

public class SerialPortService(IOptionService option, IErrorLogService errorLog)
    : ISerialPortService
{
    private readonly Dictionary<string, int> _flags = new Dictionary<string, int>();
    private readonly Dictionary<string, SerialPort> _serialPorts = new Dictionary<string, SerialPort>();
    private string _ver = "1.0.0";
    // 下位机返回的标志
    private int flag = -1;
    private bool rx;

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
                            var arr = bytes.Skip(5).Take(bytes.Length - 9).ToArray();
                            // arr to string
                            var str = Encoding.ASCII.GetString(arr).Split("+")[0];
                            Log.Information("下位机版本：" + str);
                            _ver = str;
                        }
                        catch (Exception e)
                        {
                            Log.Error(e, e.Message);
                        }
                    }
                        break;
                    case 0xA0:
                    {
                        if (bytes[5] == 0x00)
                        {
                            flag = 0;
                        }
                        else
                        {
                            flag = -1;
                        }
                    }
                        break;
                    case 0xA1:
                    {
                        if (bytes[5] == 0x00)
                        {
                            flag = 1;
                        }
                        else
                        {
                            flag = -1;
                        }
                    }
                        break;
                    case 0xA2:
                    {
                        if (bytes[5] == 0x00)
                        {
                            flag = 2;
                        }
                        else
                        {
                            flag = -1;
                        }
                    }
                        break;
                    case 0xA3:
                    {
                        if (bytes[5] == 0x01)
                        {
                            flag = -1;
                        }
                        rx = true;
                    }
                        break;
                    case 0xA4:
                    {
                        if (bytes[5] == 0x00)
                        {
                            flag = 4;
                        }
                        else
                        {
                            flag = -1;
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
        SetFlag("led", 0);

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
        return _flags.TryGetValue(alias, out var flag1) ? flag1 : 0;
    }

    #endregion

    #region 设置标志

    public void SetFlag(string alias, int flag1)
    {
        _flags[alias] = flag1;
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
            if (_serialPorts.TryGetValue(alias, out var port))
            {
                port.Write(bytes, 0, bytes.Length);
                Log.Information(alias + " 发送: " + BitConverter.ToString(bytes));
            }
            else
            {
                Log.Error("找不到串口: " + alias);
            }
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

    #region 下位机升级

    public async Task EmbeddedUpdate(string path)
    {
        const int byteLength = 1024;
        var bytes = await File.ReadAllBytesAsync(path);
        var totalPackage = bytes.Length / byteLength + (bytes.Length % byteLength == 0 ? 0 : 1);

        // 准备升级
        WritePort("Com2", DefaultProtocol.UpgradePrepare().ToBytes());
        await Task.Delay(1000);
        if (flag != 0)
        {
            throw new Exception("准备升级失败");
        }

        // 发送升级数据信息
        WritePort("Com2", DefaultProtocol.UpgradeData(totalPackage, bytes.Length).ToBytes());
        await Task.Delay(1000);
        if (flag != 1)
        {
            throw new Exception("发送升级数据信息失败");
        }

        // 地址擦除
        WritePort("Com2", DefaultProtocol.EraseAddress(byteLength).ToBytes());
        await Task.Delay(2000);
        if (flag != 2)
        {
            throw new Exception("地址擦除失败");
        }

        // 发送升级数据
        for (var i = 0; i < totalPackage; i++)
        {
            rx = false;
            var data = bytes.Skip(i * byteLength).Take(byteLength).ToArray();
            WritePort("Com2", DefaultProtocol.WriteData(i, data).ToBytes());
            var count = 10;
            while (count > 0 && !rx)
            {
                await Task.Delay(100);
                count--;
            }
            if (!rx)
            {
                throw new Exception("发送升级数据失败");
            }
        }

        // 升级完成
        WritePort("Com2", DefaultProtocol.UpgradeEnd().ToBytes());
        await Task.Delay(300);
        if (flag != 4)
        {
            throw new Exception("升级失败");
        }
    }

    #endregion

    #region 打开串口

    private async Task OpenPort(string alias)
    {
        if (_serialPorts.ContainsKey(alias))
        {
            Log.Warning("已经打开串口: " + alias);
            return;
        }

        var portName = await option.GetOptionValueAsync(alias);
        if (portName == null)
        {
            Log.Error("找不到串口号: " + alias);
            return;
        }

        if (_serialPorts.Any(kv => kv.Value.PortName == portName))
        {
            Log.Warning("已经打开串口: " + portName);
            return;
        }

        SerialPort serialPort = new SerialPort(portName, 115200, Parity.None, 8, StopBits.One);
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