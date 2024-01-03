using System.Management;

namespace Exposure.Api.Contracts.Services;

public interface IUsbService
{
    Task InitializeAsync();

    DriveInfo[] GetUsbDrives();

    DriveInfo? GetDefaultUsbDrive();

    bool IsUsbAttached();

    void DeviceInsertedEvent(object sender, EventArrivedEventArgs e);

    void DeviceRemovedEvent(object sender, EventArrivedEventArgs e);
}