namespace Exposure.Api.Contracts.Services;

public interface ISerialPortService
{
    string[] GetPorts();

    bool OpenPort(string portName, int baudRate);

    void ClosePort(string portName);

    void WritePort(string portName, byte[] bytes);

    string? ReadPort(string portName);
}