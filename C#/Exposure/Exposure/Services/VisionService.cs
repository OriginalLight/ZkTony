using System.Runtime.InteropServices;
using System.Text;
using Exposure.Contracts.Services;
using Exposure.Helpers;
using Exposure.Logging;
using OpenCvSharp;

namespace Exposure.Services;

public class VisionService : IVisionService
{
    //存储用来判断是否已经格式化
    private bool _isInit;
    private IntPtr _deviceId = IntPtr.Zero;
    private readonly ILocalSettingsService _localSettingsService;

    public VisionService(ILocalSettingsService localSettingsService)
    {
        _localSettingsService = localSettingsService;
    }

    public async Task InitAsync()
    {
        if (_isInit)
        {
            return;
        }

        VisionHelper.Init();
        GlobalLog.Logger?.ReportInfo("初始化SDK");

        _isInit = true;

        await Task.CompletedTask;
    }

    public async Task UninitAsync()
    {
        if (!_isInit)
        {
            return;
        }

        VisionHelper.Uninit();
        GlobalLog.Logger?.ReportInfo("释放SDK");

        _isInit = false;
        _deviceId = IntPtr.Zero;

        await Task.CompletedTask;
    }

    public async Task ConnectAsync()
    {
        if (!_isInit)
        {
            return;
        }

        if (_deviceId != IntPtr.Zero)
        {
            return;
        }
        var num = VisionHelper.SearchforDevice();
        if (num == 0)
        {
            GlobalLog.Logger?.ReportInfo("未找到设备");
            _deviceId = IntPtr.Zero;
            return;
        }

        var deviceId = VisionHelper.GetDeviceID(0);
        // DeviceInfo
        var device = new VisionHelper.SDeviceInfo();
        VisionHelper.GetDeviceInfo(deviceId, ref device);
        GlobalLog.Logger?.ReportInfo($"P_MAC：{Marshal.PtrToStringAnsi(device.pMAC)}");
        // LocalInfo
        var local = new VisionHelper.SNICInfo();
        VisionHelper.GetLocalNICInfo(deviceId, ref local);
        GlobalLog.Logger?.ReportInfo($"L_MAC：{Marshal.PtrToStringAnsi(local.pMAC)}");

        // 判断Device的网段是否与Local的网段一致
        var deviceIp = Marshal.PtrToStringAnsi(device.pIP) ?? string.Empty;
        var localIp = Marshal.PtrToStringAnsi(local.pIP) ?? string.Empty;
        var deviceIpArray = deviceIp.Split('.');
        var localIpArray = localIp.Split('.');
        if (deviceIpArray[0] != localIpArray[0] || deviceIpArray[1] != localIpArray[1] || deviceIpArray[2] != localIpArray[2])
        {
            GlobalLog.Logger?.ReportInfo("设备与本地网段不一致");
            // ForceIP
            var forceIp = VisionHelper.ForceIP(deviceId, device.pIP, local.pMask, device.pGateway);
            if (forceIp)
            {
                GlobalLog.Logger?.ReportInfo("ForceIP 成功");
            }
            else
            {
                GlobalLog.Logger?.ReportError("ForceIP 失败");
                return;
            }
        }

        // OpenDevice
        if (VisionHelper.OpenDevice(deviceId))
        {
            GlobalLog.Logger?.ReportInfo("OpenDevice 成功");
        }
        else
        {
            GlobalLog.Logger?.ReportError("OpenDevice 失败");
        }

        _deviceId = deviceId;

        await Task.CompletedTask;
    }

    public async Task DisconnectAsync()
    {
        if (_deviceId == IntPtr.Zero)
        {
            return;
        }
        VisionHelper.CloseDevice(_deviceId);

        await Task.CompletedTask;
    }

    public async Task StartCaptureAsync()
    {
        ushort iFrameId = 0;
        var iWidth = 0;
        var iHeight = 0;
        var iPixelBits = 0;
        if (_deviceId == IntPtr.Zero)
        {
            return;
        }

        var steam = VisionHelper.CreateStream(_deviceId!);
        GlobalLog.Logger?.ReportInfo(steam != IntPtr.Zero
            ? "CreateStream 成功"
            : $"CreateStream 失败 {Marshal.PtrToStringAnsi(VisionHelper.GetLastErrorText())}");
        var start = VisionHelper.StartStream(steam, true, null, IntPtr.Zero);
        GlobalLog.Logger?.ReportInfo(start
            ? "StartStream 成功"
            : $"StartStream 失败 {Marshal.PtrToStringAnsi(VisionHelper.GetLastErrorText())}");
        var frame = VisionHelper.GetRawFrame(steam, ref iFrameId, ref iWidth, ref iHeight, ref iPixelBits);
        if (frame != IntPtr.Zero)
        {
            var iPixelNum = iWidth * iHeight;
            var bytes = new byte[iPixelNum * sizeof(ushort)];
            for (var k = 0; k < iPixelNum; k++)
            {
                var iPixelValue = VisionHelper.GetRawPixelValue(frame, iPixelBits, k);
                var pixelBytes = BitConverter.GetBytes(iPixelValue);
                Array.Copy(pixelBytes, 0, bytes, k * sizeof(ushort), sizeof(ushort));
            }
            var rawImage = new Mat(iHeight, iWidth, MatType.CV_16U, bytes);
            var root = await _localSettingsService.ReadSettingAsync<string>("Storage") ?? Environment.GetFolderPath(Environment.SpecialFolder.MyDocuments);
            var now = DateTime.Now.ToString("yyyy-MM-dd");
            if (!Directory.Exists(Path.Combine(root, now)))
            {
                Directory.CreateDirectory(Path.Combine(root, now));
            }
            var fileName = DateTime.Now.ToString("HH-mm-ss") + ".tiff";
            var outImage = Path.Combine(Path.Combine(root, now), fileName);
            rawImage.SaveImage(outImage);
            GlobalLog.Logger?.ReportInfo($"GetRawFrame 成功 {iWidth}x{iHeight}");
        }
        else
        {
            GlobalLog.Logger?.ReportInfo("GetRawFrame 失败");
        }

        VisionHelper.StopStream(steam);
        VisionHelper.DestroyStream(steam);

    }

    public bool SetAttributeIntAsync(long value, string attribute)
    {
        if (!_isInit)
        {
            return false;
        }

        if (_deviceId == IntPtr.Zero)
        {
            return false;
        }
        var att = Marshal.StringToHGlobalAnsi(attribute);
        if (VisionHelper.SetAttrInt(_deviceId, att, value, 0))
        {
            GlobalLog.Logger?.ReportInfo($"SetAttrInt {attribute} 成功 {value}");
            return true;
        }

        GlobalLog.Logger?.ReportError($"SetAttrInt {attribute} 失败 {value}");
        return false;
    }

    public long GetAttributeIntAsync(string attribute)
    {
        if (!_isInit)
        {
            return -1L;
        }

        if (_deviceId == IntPtr.Zero)
        {
            return -1L;
        }

        var att = Marshal.StringToHGlobalAnsi(attribute);
        var iValue = 0L;
        if (VisionHelper.GetAttrInt(_deviceId, att, ref iValue, 0))
        {
            return iValue;
        }

        GlobalLog.Logger?.ReportError($"GetAttrInt {attribute} 失败");
        return -1L;
    }

    public bool SetAttributeFloatAsync(double value, string attribute)
    {
        if (!_isInit)
        {
            return false;
        }

        if (_deviceId == IntPtr.Zero)
        {
            return false;
        }
        var att = Marshal.StringToHGlobalAnsi(attribute);
        if (VisionHelper.SetAttrFloat(_deviceId, att, value, 0))
        {
            return true;
        }

        GlobalLog.Logger?.ReportError($"SetAttrFloat {attribute} 失败 {value}");
        return false;
    }

    public double GetAttributeFloatAsync(string attribute)
    {
        if (!_isInit)
        {
            return -1.0;
        }

        if (_deviceId == IntPtr.Zero)
        {
            return -1.0;
        }

        var att = Marshal.StringToHGlobalAnsi(attribute);
        var dValue = 0.0;
        if (VisionHelper.GetAttrFloat(_deviceId, att, ref dValue, 0))
        {
            return dValue;
        }

        GlobalLog.Logger?.ReportError($"GetAttrFloat {attribute} 失败");
        return -1.0;
    }

    public string GetAttributeStringAsync(string attribute)
    {
        if (!_isInit)
        {
            return string.Empty;
        }
        if (_deviceId == IntPtr.Zero)
        {
            return string.Empty;
        }

        var att = Marshal.StringToHGlobalAnsi(attribute);
        var sValue = new StringBuilder(128);
        if (VisionHelper.GetAttrString(_deviceId, att, sValue, 0))
        {
            return sValue.ToString();
        }

        GlobalLog.Logger?.ReportError($"GetAttrString {attribute} 失败");
        return string.Empty;
    }

    public async Task GetAttributeAsync()
    {
        if (!_isInit)
        {
            return;
        }

        if (_deviceId == IntPtr.Zero)
        {
            return;
        }

        var number = VisionHelper.GetNumberOfAttribute(_deviceId);
        GlobalLog.Logger?.ReportInfo($"GetNumberOfAttribute {number}");
        for (var i = 0; i < number; i++)
        {
            var attrName = VisionHelper.GetAttributeName(_deviceId, (uint)i);
            if (attrName == IntPtr.Zero)
            {
                continue;
            }

            var attrType = 0;
            if (!VisionHelper.GetAttributeType(_deviceId, attrName, ref attrType))
            {
                continue;
            }

            switch (attrType)
            {
                case 0:
                    {
                        long iValue = 0;
                        if (VisionHelper.GetAttrMaxInt(_deviceId, attrName, ref iValue))
                        {
                            GlobalLog.Logger?.ReportInfo($"GetAttrMaxInt {Marshal.PtrToStringAnsi(attrName)} 成功 {iValue}");
                        }
                        else
                        {
                            GlobalLog.Logger?.ReportError($"GetAttrMaxInt {Marshal.PtrToStringAnsi(attrName)} 失败");
                        }

                        if (VisionHelper.GetAttrMinInt(_deviceId, attrName, ref iValue))
                        {
                            GlobalLog.Logger?.ReportInfo($"GetAttrMinInt {Marshal.PtrToStringAnsi(attrName)} 成功 {iValue}");
                        }
                        else
                        {
                            GlobalLog.Logger?.ReportError($"GetAttrMinInt {Marshal.PtrToStringAnsi(attrName)} 失败");
                        }

                        if (VisionHelper.GetAttrInt(_deviceId, attrName, ref iValue, 0))
                        {
                            GlobalLog.Logger?.ReportInfo($"GetAttrInt {Marshal.PtrToStringAnsi(attrName)} 成功 {iValue}");
                        }
                        else
                        {
                            GlobalLog.Logger?.ReportError($"GetAttrInt {Marshal.PtrToStringAnsi(attrName)} 失败");
                        }
                    }
                    break;
                case 1:
                    {
                        double dValue = 0;
                        if (VisionHelper.GetAttrMaxFloat(_deviceId, attrName, ref dValue))
                        {
                            GlobalLog.Logger?.ReportInfo($"GetAttrMaxFloat {Marshal.PtrToStringAnsi(attrName)} 成功 {dValue}");
                        }
                        else
                        {
                            GlobalLog.Logger?.ReportError($"GetAttrMaxFloat {Marshal.PtrToStringAnsi(attrName)} 失败");
                        }

                        if (VisionHelper.GetAttrMinFloat(_deviceId, attrName, ref dValue))
                        {
                            GlobalLog.Logger?.ReportInfo($"GetAttrMinFloat {Marshal.PtrToStringAnsi(attrName)} 成功 {dValue}");
                        }
                        else
                        {
                            GlobalLog.Logger?.ReportError($"GetAttrMinFloat {Marshal.PtrToStringAnsi(attrName)} 失败");
                        }

                        if (VisionHelper.GetAttrFloat(_deviceId, attrName, ref dValue, 0))
                        {
                            GlobalLog.Logger?.ReportInfo($"GetAttrFloat {Marshal.PtrToStringAnsi(attrName)} 成功 {dValue}");
                        }
                        else
                        {
                            GlobalLog.Logger?.ReportError($"GetAttrFloat {Marshal.PtrToStringAnsi(attrName)} 失败");
                        }
                    }
                    break;
                case 2:
                    {
                        var sValue = new StringBuilder(128);
                        if (VisionHelper.GetAttrString(_deviceId, attrName, sValue, 0))
                        {
                            GlobalLog.Logger?.ReportInfo($"GetAttrString {Marshal.PtrToStringAnsi(attrName)} 成功 {sValue}");
                        }
                        else
                        {
                            GlobalLog.Logger?.ReportError($"GetAttrString {Marshal.PtrToStringAnsi(attrName)} 失败");
                        }
                    }
                    break;
                case 3:
                    {
                        var iEntryNumber = VisionHelper.GetNumberOfEntry(_deviceId, attrName);
                        for (var k = 0; k < iEntryNumber; k++)
                        {
                            var id = VisionHelper.GetEntryID(_deviceId, attrName, (uint)k);
                            var pName = VisionHelper.GetEntryName(_deviceId, attrName, (uint)k);
                            if (id != -1 && pName != IntPtr.Zero)
                            {
                                Console.WriteLine("      {0:D}={1}", id, Marshal.PtrToStringAnsi(pName));
                            }
                            else
                            {
                                Console.WriteLine("	   Fail to get the entry with index={0:D}", k);
                            }
                        }

                        long iEntryId = 0;
                        if (VisionHelper.GetAttrInt(_deviceId, attrName, ref iEntryId, 0))
                        {
                            Console.WriteLine("    Current Value={0:D} text={1}", iEntryId, Marshal.PtrToStringAnsi(VisionHelper.GetEntryNameByID(_deviceId, attrName, (uint)iEntryId)));
                        }
                        else
                        {
                            Console.WriteLine("    Current Value=Error:{0}", Marshal.PtrToStringAnsi(VisionHelper.GetLastErrorText()));
                        }
                        if (VisionHelper.SetAttrInt(_deviceId, attrName, iEntryId, 0))
                        {
                            Console.WriteLine("    New Value={0:D}", iEntryId);
                        }
                        else
                        {
                            Console.WriteLine("    Current Value=Error:{0}", Marshal.PtrToStringAnsi(VisionHelper.GetLastErrorText()));
                        }
                    }
                    break;
                default:
                    {
                        GlobalLog.Logger?.ReportError($"UnSupport AttrType {attrType}");
                    }
                    break;
            }
        }

        await Task.CompletedTask;
    }
}