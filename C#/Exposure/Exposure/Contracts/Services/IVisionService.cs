namespace Exposure.Contracts.Services;

public interface IVisionService
{
    bool IsInitialized
    {
        get;
    }

    bool IsConnected
    {
        get;
    }

    Task InitAsync();

    Task UninitAsync();

    Task ConnectAsync();

    Task DisconnectAsync();

    Task CalibrateAsync(IProgress<int> progress);

    Task ShootingAsync(IProgress<int> progress, int exposureTime, CancellationToken token);

    bool SetAttributeInt(long value, string attribute);

    long GetAttributeInt(string attribute);

    bool SetAttributeFloat(double value, string attribute);

    double GetAttributeFloat(string attribute);

    string GetAttributeString(string attribute);
}