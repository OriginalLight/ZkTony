using System.Runtime.InteropServices;
using System.Text;
using Exposure.Contracts.Services;
using Exposure.Helpers;
using Exposure.Logging;
using OpenCvSharp;

namespace Exposure.Services;

public class VisionService : IVisionService
{
    private readonly ILocalSettingsService _localSettingsService;

    private IntPtr _deviceId = IntPtr.Zero;

    public VisionService(ILocalSettingsService localSettingsService)
    {
        _localSettingsService = localSettingsService;
    }

    public bool IsInitialized
    {
        get;
        private set;
    }

    public bool IsConnected => _deviceId != IntPtr.Zero;

    public async Task InitAsync()
    {
        if (IsInitialized)
        {
            return;
        }

        VisionHelper.Init();
        GlobalLog.Logger?.ReportInfo("初始化SDK");

        IsInitialized = true;

        await Task.CompletedTask;
    }

    public async Task UninitAsync()
    {
        if (!IsInitialized)
        {
            return;
        }

        VisionHelper.Uninit();
        GlobalLog.Logger?.ReportInfo("释放SDK");

        IsInitialized = false;
        _deviceId = IntPtr.Zero;

        await Task.CompletedTask;
    }

    public async Task ConnectAsync()
    {
        if (!IsInitialized)
        {
            return;
        }

        if (_deviceId != IntPtr.Zero)
        {
            return;
        }

        if (VisionHelper.SearchforDevice() == 0)
        {
            GlobalLog.Logger?.ReportInfo("未找到设备");
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
        if (deviceIpArray[0] != localIpArray[0] || deviceIpArray[1] != localIpArray[1] ||
            deviceIpArray[2] != localIpArray[2])
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

    public async Task CalibrateAsync(IProgress<int> progress)
    {
        // 验证是否已经初始化
        if (!IsInitialized)
        {
            progress.Report(0);
            return;
        }
        
        progress.Report(5);

        // 验证是否已经连接
        if (_deviceId == IntPtr.Zero)
        {
            progress.Report(0);
            return;
        }
        
        progress.Report(10);
        
        //TODO 关闭灯光
        progress.Report(20);
        
        if (!VisionHelper.CalibrateCapture(_deviceId, 0))
        {
            progress.Report(0);
            return;
        }
        progress.Report(30);
        
        if (!VisionHelper.OpenCalibrate(_deviceId, 0))
        {
            progress.Report(0);
            return;    
        }
        progress.Report(40);
        
        // TODO 打开灯光
        progress.Report(50);

        if (!VisionHelper.CalibrateCapture(_deviceId, 0))
        {
            progress.Report(0);
            return;
        }
        progress.Report(60);
        
        // TODO 关闭灯光
        progress.Report(70);

        if (!VisionHelper.CalibrateBright(_deviceId, 0, IntPtr.Zero))
        {
            progress.Report(0);
            return;
        }
        progress.Report(80);

        if (!VisionHelper.DownloadCalDataToDevice(_deviceId, 0, 0, true, IntPtr.Zero, false))
        {
            progress.Report(0);
            return;
        }
        progress.Report(90);

        VisionHelper.CloseCalibrate(_deviceId);
        progress.Report(0);

        await Task.CompletedTask;
    }

    public async Task ShootingAsync(IProgress<int> progress, int exposureTime, CancellationToken token)
    {
        var iHeight = 0;
        var iWidth = 0;
        var darkBytes = new List<byte[]>();

        // 验证是否已经初始化
        if (!IsInitialized)
        {
            progress.Report(0);
            return;
        }
        
        progress.Report(2);

        // 验证是否已经连接
        if (_deviceId == IntPtr.Zero)
        {
            progress.Report(0);
            return;
        }
        
        progress.Report(5);
        
        // 创建文件夹
        var root = await _localSettingsService.ReadSettingAsync<string>("Storage") ??
                   Environment.GetFolderPath(Environment.SpecialFolder.MyDocuments);
        var now = DateTime.Now.ToString("yyyy-MM-dd");
        if (!Directory.Exists(Path.Combine(root, now)))
        {
            Directory.CreateDirectory(Path.Combine(root, now));
        }

        var fileName = DateTime.Now.ToString("HH-mm-ss");

        progress.Report(10);
        
        // 创建流
        var steam = VisionHelper.CreateStream(_deviceId);
        if (steam == IntPtr.Zero)
        {
            progress.Report(0);
            return;
        }
        
        progress.Report(15);
        
        // TODO 关闭灯光
        progress.Report(20);


        // 最大曝光时间1500ms计算需要几次曝光
        var count = exposureTime / 1500;
        if (exposureTime % 1500 > 0)
        {
            count++;
        }
        var avg = exposureTime / count;
        
        // 设置曝光时间
        if (!SetAttributeFloat(avg, "ExposureTime"))
        {
            progress.Report(0);
            return;
        }
        
        for(var i = 0; i < count; i++)
        {
            if (token.IsCancellationRequested)
            {
                progress.Report(0);
                return;
            }
            darkBytes.Add(ShootingOnce(steam, ref iWidth, ref iHeight));
            progress.Report(20 + (40 * (i + 1) / count));
        }

        // OpenCV 多次曝光合成一张
        var dark = new Mat(iHeight, iWidth, MatType.CV_16U, new Scalar(0));
        var mats = darkBytes.Select(bytes => new Mat(iHeight, iWidth, MatType.CV_16U, bytes)).ToList();
        // 将多个 Mat 对象相加
        foreach (var m in mats)
        {
            Cv2.Add(dark, m, dark);
        }
        var outDarkImage = Path.Combine(Path.Combine(root, now), fileName + "-D.tiff");
        dark.SaveImage(outDarkImage);
        
        progress.Report(65);
        
        if (token.IsCancellationRequested)
        {
            progress.Report(0);
            return;
        }
        
        // TODO 打开灯光
        progress.Report(70);
        // 设置曝光时间
        if (!SetAttributeFloat(100, "ExposureTime"))
        {
            progress.Report(0);
            return;
        }
        
        var lightBytes = ShootingOnce(steam, ref iWidth, ref iHeight);
        
        // TODO 关闭灯光
        progress.Report(80);
        
        var light = new Mat(iHeight, iWidth, MatType.CV_16U, lightBytes);
        var outLightImage = Path.Combine(Path.Combine(root, now), fileName + "-L.tiff");
        light.SaveImage(outLightImage);
        
        progress.Report(85);
        
        if (token.IsCancellationRequested)
        {
            progress.Report(0);
            return;
        }

        // dark + light 合成一张图片
        var combine = new Mat(iHeight, iWidth, MatType.CV_16U, new Scalar(0));
        Cv2.Add(dark, light, combine);
        var outCombineImage = Path.Combine(Path.Combine(root, now), fileName + "-C.tiff");
        combine.SaveImage(outCombineImage);
        
        progress.Report(95);
        
        VisionHelper.DestroyStream(steam);
        
        progress.Report(0);
    }

    private byte[] ShootingOnce(nint steam, ref int width, ref int height)
    {
        ushort iFrameId = 0;
        var iPixelBits = 0;
        var bytes = Array.Empty<byte>();
        
        // 开始流
        if (!VisionHelper.StartStream(steam, true, null, IntPtr.Zero))
        {
            GlobalLog.Logger?.ReportError("StartStream 失败");
            VisionHelper.DestroyStream(steam);
            return bytes;
        }
            
        // 获取帧
        var frame = VisionHelper.GetRawFrame(steam, ref iFrameId, ref width, ref height, ref iPixelBits);
        if (frame != IntPtr.Zero)
        {
            var iPixelNum = width * height;
            bytes = new byte[iPixelNum * sizeof(ushort)];
            for (var k = 0; k < iPixelNum; k++)
            {
                var iPixelValue = VisionHelper.GetRawPixelValue(frame, iPixelBits, k);
                var pixelBytes = BitConverter.GetBytes(iPixelValue);
                Array.Copy(pixelBytes, 0, bytes, k * sizeof(ushort), sizeof(ushort));
            }
            
            GlobalLog.Logger?.ReportInfo($"GetRawFrame 成功 {width}x{height}");
        }
        else
        {
            GlobalLog.Logger?.ReportInfo("GetRawFrame 失败");
        }

        VisionHelper.StopStream(steam);
        return bytes;

    }

    public bool SetAttributeInt(long value, string attribute)
    {
        if (!IsInitialized)
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

    public long GetAttributeInt(string attribute)
    {
        if (!IsInitialized)
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

    public bool SetAttributeFloat(double value, string attribute)
    {
        if (!IsInitialized)
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

    public double GetAttributeFloat(string attribute)
    {
        if (!IsInitialized)
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

    public string GetAttributeString(string attribute)
    {
        if (!IsInitialized)
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
        if (!IsInitialized)
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
                            GlobalLog.Logger?.ReportInfo(
                                $"GetAttrMaxFloat {Marshal.PtrToStringAnsi(attrName)} 成功 {dValue}");
                        }
                        else
                        {
                            GlobalLog.Logger?.ReportError($"GetAttrMaxFloat {Marshal.PtrToStringAnsi(attrName)} 失败");
                        }

                        if (VisionHelper.GetAttrMinFloat(_deviceId, attrName, ref dValue))
                        {
                            GlobalLog.Logger?.ReportInfo(
                                $"GetAttrMinFloat {Marshal.PtrToStringAnsi(attrName)} 成功 {dValue}");
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
                            Console.WriteLine("    Current Value={0:D} text={1}", iEntryId,
                                Marshal.PtrToStringAnsi(VisionHelper.GetEntryNameByID(_deviceId, attrName,
                                    (uint)iEntryId)));
                        }
                        else
                        {
                            Console.WriteLine("    Current Value=Error:{0}",
                                Marshal.PtrToStringAnsi(VisionHelper.GetLastErrorText()));
                        }

                        if (VisionHelper.SetAttrInt(_deviceId, attrName, iEntryId, 0))
                        {
                            Console.WriteLine("    New Value={0:D}", iEntryId);
                        }
                        else
                        {
                            Console.WriteLine("    Current Value=Error:{0}",
                                Marshal.PtrToStringAnsi(VisionHelper.GetLastErrorText()));
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