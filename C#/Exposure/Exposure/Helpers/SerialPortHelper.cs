using System.IO.Ports;
using Exposure.Logging;

namespace Exposure.Helpers;
public class SerialPortHelper
{
    private static SerialPort? _serialPort;

    public static string[] GetPortNames()
    {
        return SerialPort.GetPortNames();
    }

    public static void Open(string portName, int baudRate)
    {
        _serialPort = new SerialPort
        {
            PortName = portName,
            BaudRate = baudRate,
            DataBits = 8,
            Parity = Parity.None,
            StopBits = StopBits.One,
            ReadTimeout = 1000,
            WriteTimeout = 1000
        };
        _serialPort.Open();
        _serialPort.DataReceived += ReceiveData;
    }

    public static void Close()
    {
        _serialPort?.Close();
    }

    public static void Write(byte[] bytes)
    {
        if (_serialPort is not { IsOpen: true })
        {
            return;
        }
        _serialPort.Write(bytes, 0, bytes.Length);

        GlobalLog.Logger?.ReportInfo($"发送数据：{bytes}");
    }

    private static void ReceiveData(object sender, SerialDataReceivedEventArgs e)
    {
        var serialPort = (SerialPort)sender;

        var bytesToRead = serialPort.BytesToRead;
        var recvData = new byte[bytesToRead];

        serialPort.Read(recvData, 0, bytesToRead);

        GlobalLog.Logger?.ReportInfo($"接收数据：{recvData}");
    }
}
