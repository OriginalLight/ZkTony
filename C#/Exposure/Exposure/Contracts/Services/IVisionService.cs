namespace Exposure.Contracts.Services;

public interface IVisionService
{
    Task InitAsync();

    Task UninitAsync();

    string GetVisionText();

    int SearchforDevice();
}