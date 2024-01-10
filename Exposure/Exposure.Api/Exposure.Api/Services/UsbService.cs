using System.Management;
using Exposure.Api.Contracts.Services;

namespace Exposure.Api.Services;

public class UsbService : IUsbService
{
    private readonly ManagementEventWatcher _insertWatcher;
    private readonly ILogger<UsbService> _logger;
    private readonly ManagementEventWatcher _removeWatcher;

    public UsbService(ILogger<UsbService> logger)
    {
        _logger = logger;
        // Add insert event watcher.
        _insertWatcher = new ManagementEventWatcher();
        _insertWatcher.Query = new WqlEventQuery("SELECT * FROM Win32_DeviceChangeEvent WHERE EventType = 2");
        _insertWatcher.Start();

        // Add remove event watcher.
        _removeWatcher = new ManagementEventWatcher();
        _removeWatcher.Query = new WqlEventQuery("SELECT * FROM Win32_DeviceChangeEvent WHERE EventType = 3");
        _removeWatcher.Start();
    }

    public void DeviceInsertedEvent(object sender, EventArrivedEventArgs e)
    {
        // 获取到当前插入的什么
        _logger.LogInformation("外设插入");
    }

    public void DeviceRemovedEvent(object sender, EventArrivedEventArgs e)
    {
        _logger.LogInformation("外设移除");
    }

    public Task InitializeAsync()
    {
        _insertWatcher.EventArrived += DeviceInsertedEvent;
        _removeWatcher.EventArrived += DeviceRemovedEvent;

        return Task.CompletedTask;
    }

    public DriveInfo[] GetUsbDrives()
    {
        var allDrives = DriveInfo.GetDrives();

        var usbDrives = Array.FindAll(allDrives, d => d.DriveType == DriveType.Removable);

        return usbDrives;
    }

    public DriveInfo? GetDefaultUsbDrive()
    {
        var usbDrives = GetUsbDrives();

        return usbDrives.Length > 0 ? usbDrives[0] : null;
    }

    public bool IsUsbAttached()
    {
        var usbDrives = GetUsbDrives();

        return usbDrives.Length > 0;
    }
}