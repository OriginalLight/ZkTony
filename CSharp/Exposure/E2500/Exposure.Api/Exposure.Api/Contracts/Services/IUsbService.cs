using System.Management;

namespace Exposure.Api.Contracts.Services;

public interface IUsbService
{
    /// <summary>
    ///     初始化
    /// </summary>
    /// <returns></returns>
    Task InitializeAsync();

    /// <summary>
    ///     获取所有的USB设备
    /// </summary>
    /// <returns></returns>
    DriveInfo[] GetUsbDrives();

    /// <summary>
    ///     获取默认的USB设备
    /// </summary>
    /// <returns></returns>
    DriveInfo? GetDefaultUsbDrive();

    /// <summary>
    ///     判断是否有USB设备插入
    /// </summary>
    /// <returns></returns>
    bool IsUsbAttached();

    /// <summary>
    ///     外设插入
    /// </summary>
    /// <param name="sender"></param>
    /// <param name="e"></param>
    void DeviceInsertedEvent(object sender, EventArrivedEventArgs e);

    /// <summary>
    ///     外设移除
    /// </summary>
    /// <param name="sender"></param>
    /// <param name="e"></param>
    void DeviceRemovedEvent(object sender, EventArrivedEventArgs e);
}