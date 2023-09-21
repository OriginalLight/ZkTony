namespace Exposure.Contracts.Services;

public interface IVisionService
{
    Task InitAsync();

    Task UninitAsync();

    Task ConnectAsync();
    
    Task DisconnectAsync();
    
    Task StartCaptureAsync();
    
    bool SetAttributeIntAsync(long value, string attribute);
    
    long GetAttributeIntAsync(string attribute);
    
    bool SetAttributeFloatAsync(double value, string attribute);
    
    double GetAttributeFloatAsync(string attribute);
    
    string GetAttributeStringAsync(string attribute);
}