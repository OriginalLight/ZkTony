namespace Exposure.Contracts.Services;

public interface IVisionService
{
    Task InitAsync();
    
    string? GetVisionText();
}