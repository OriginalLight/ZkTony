using System.Management;
using Exposure.Api.Contracts.Services;

namespace Exposure.Api.Services;

public class UsbService(ILogger<UsbService> logger) : IUsbService
{
    private readonly ManagementEventWatcher _insertWatcher = new();
    private readonly ManagementEventWatcher _removeWatcher = new();

    #region 外设插入

    public void DeviceInsertedEvent(object sender, EventArrivedEventArgs e)
    {
        // 获取到当前插入的什么
        logger.LogInformation("外设插入");
    }

    #endregion

    #region 外设移除

    public void DeviceRemovedEvent(object sender, EventArrivedEventArgs e)
    {
        logger.LogInformation("外设移除");
    }

    #endregion

    #region 初始化

    public Task InitializeAsync()
    {
        // Add insert event watcher.
        _insertWatcher.Query = new WqlEventQuery("SELECT * FROM Win32_DeviceChangeEvent WHERE EventType = 2");
        _insertWatcher.Start();

        // Add remove event watcher.
        _removeWatcher.Query = new WqlEventQuery("SELECT * FROM Win32_DeviceChangeEvent WHERE EventType = 3");
        _removeWatcher.Start();

        _insertWatcher.EventArrived += DeviceInsertedEvent;
        _removeWatcher.EventArrived += DeviceRemovedEvent;

        return Task.CompletedTask;
    }

    #endregion

    #region 获取所有的USB设备

    public DriveInfo[] GetUsbDrives()
    {
        var allDrives = DriveInfo.GetDrives();

        var usbDrives = Array.FindAll(allDrives, d => d.DriveType is DriveType.Removable);

        return usbDrives;
    }

    #endregion

    #region 获取默认的USB设备

    public DriveInfo? GetDefaultUsbDrive()
    {
        var usbDrives = GetUsbDrives();

        return usbDrives.Length > 0 ? usbDrives[0] : null;
    }

    #endregion

    #region 是否有USB设备

    public bool IsUsbAttached()
    {
        var usbDrives = GetUsbDrives();

        return usbDrives.Length > 0;
    }

    #endregion
}