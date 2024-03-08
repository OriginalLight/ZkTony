using System.Runtime.InteropServices;
using Exposure.Api.Contracts.Services;
using Exposure.Api.Core.SerialPort.Default;
using Exposure.Api.Models;
using Exposure.Api.Utils;
using OpenCvSharp;

namespace Exposure.Api.Services;

public class CameraService : ICameraService
{
    private const uint ExpoTime = 1500;
    private readonly IPictureService _picture;

    private readonly List<Picture> _pictureList = [];
    private readonly ISerialPortService _serialPort;
    private readonly IUserService _user;
    private string _flag = "preview";
    private Mat? _mat;
    private Nncam? _nncam;
    private int _seq;
    private int _target;

    #region 构造函数

    public CameraService(ISerialPortService serialPort, IPictureService picture, IUserService user)
    {
        _serialPort = serialPort;
        _picture = picture;
        _user = user;
    }

    #endregion

    #region 初始化

    public void Init()
    {
        if (_nncam != null) return;

        try
        {
            var arr = Nncam.EnumV2();
            if (arr.Length == 0) throw new Exception("未找到设备");

            // 打开设备
            _nncam = Nncam.Open(arr[0].id);
            if (_nncam == null) throw new Exception("打开设备失败");

            // 设置参数
            if (!_nncam.put_Temperature(-150)) throw new Exception("设置温度失败");
            if (!_nncam.put_Option(Nncam.eOPTION.OPTION_TRIGGER, 1)) throw new Exception("设置模式失败");
            if (!_nncam.put_AutoExpoEnable(false)) throw new Exception("参数自动曝光设置失败");
            if (!_nncam.put_Chrome(true)) throw new Exception("设置单色失败");
            if (!_nncam.put_Option(Nncam.eOPTION.OPTION_BITDEPTH, 1)) throw new Exception("设置位深失败");
            if (!_nncam.put_ExpoAGain(3000)) throw new Exception("设置增益失败");

            // 设置回调
            if (!SetCallBack()) throw new Exception("设置回调失败");
        }
        catch (Exception)
        {
            _nncam?.Close();
            _nncam = null;
            _serialPort.WritePort("Com1", DefaultProtocol.LedRed().ToBytes());
            _serialPort.SetFlag("led", 5);
            throw;
        }
    }

    #endregion

    #region 停止

    public void Stop()
    {
        _nncam?.Close();
        _nncam = null;
    }

    #endregion

    #region 预览

    public async Task PreviewAsync()
    {
        Init();
        if (_nncam == null) throw new Exception("预览失败");
        // 清空队列
        _pictureList.Clear();
        // 设置flag
        _flag = "preview";
        // 目标张数
        _target = 1;

        try
        {
            //打开灯光
            _serialPort.WritePort("Com2", DefaultProtocol.OpenLight().ToBytes());
            // 延时100ms
            await Task.Delay(100);

            // 设置曝光时间
            if (!_nncam.put_ExpoTime(ExpoTime)) throw new Exception("设置曝光时间失败");
            // 触发拍摄
            if (!_nncam.Trigger(1)) throw new Exception("预览失败");
        }
        catch (Exception)
        {
            _serialPort.WritePort("Com2", DefaultProtocol.CloseLight().ToBytes());
            throw;
        }
    }

    #endregion

    #region 自动拍照

    public async Task<long> TakeAutoPhotoAsync(CancellationToken ctsToken)
    {
        Init();
        if (_nncam == null) throw new Exception("自动拍照失败");

        _mat = null;
        _flag = "sampling";
        uint targetExpo = 1000000;

        // 设置曝光时间
        if (!_nncam.put_ExpoTime(1000000)) throw new Exception("设置采样曝光时间失败");
        // 计算曝光时间
        if (!_nncam.Trigger(1)) throw new Exception("计算曝光时间失败");

        // 延时1300ms
        await Task.Delay(1300, ctsToken);

        if (_mat == null) throw new Exception("获取采样图失败");
        var gray = new Mat();
        // 转换成灰度图
        Cv2.CvtColor(_mat, gray, ColorConversionCodes.BGR2GRAY);
        var expo = CalculateExpo(gray, (int)targetExpo, 0.5);
        if (expo > 1000000L * 60L * 60L) throw new Exception("曝光时间过长");
        targetExpo = Math.Max((uint)expo, 10000);

        _mat = null;
        _pictureList.Clear();
        _target = 3;
        _seq = 0;
        _flag = "auto";

        try
        {
            //打开灯光
            _serialPort.WritePort("Com2", DefaultProtocol.OpenLight().ToBytes());
            // 延时100ms
            await Task.Delay(100, ctsToken);

            // 设置曝光时间
            if (!_nncam.put_ExpoTime(ExpoTime)) throw new Exception("设置白光曝光时间失败");
            // 触发拍摄
            if (!_nncam.Trigger(1)) throw new Exception("拍摄白光图失败");
        }
        catch (Exception)
        {
            // 关闭灯光
            _serialPort.WritePort("Com2", DefaultProtocol.CloseLight().ToBytes());
            throw;
        }

        // 延时500ms
        await Task.Delay(500, ctsToken);

        // 设置曝光时
        if (!_nncam.put_ExpoTime(targetExpo)) throw new Exception("设置曝光图曝光时间失败");

        if (!_nncam.Trigger(1)) throw new Exception("拍摄曝光图失败");

        return targetExpo;
    }

    #endregion

    #region 手动拍照

    public async Task TakeManualPhotoAsync(int exposure, int frame, CancellationToken ctsToken)
    {
        Init();
        if (_nncam == null) throw new Exception("手动拍照失败");

        _mat = null;
        _pictureList.Clear();
        _target = frame + 1;
        _seq = 0;
        _flag = "manual";

        try
        {
            //打开灯光
            _serialPort.WritePort("Com2", DefaultProtocol.OpenLight().ToBytes());
            // 延时100ms
            await Task.Delay(100, ctsToken);

            // 设置曝光时间
            if (!_nncam.put_ExpoTime(ExpoTime)) throw new Exception("设置白光曝光时间失败");
            // 触发拍摄
            if (!_nncam.Trigger(1)) throw new Exception("拍摄白光图失败");
        }
        catch (Exception)
        {
            _serialPort.WritePort("Com2", DefaultProtocol.CloseLight().ToBytes());
            throw;
        }

        // 延时500ms
        await Task.Delay(500, ctsToken);

        // 设置曝光时
        if (!_nncam.put_ExpoTime((uint)(exposure / frame))) throw new Exception("设置多帧曝光时间失败");
        // 触发拍摄
        if (!_nncam.Trigger((ushort)frame)) throw new Exception("拍摄曝光图失败");
    }

    #endregion

    #region 取消拍照

    public void CancelTask()
    {
        Init();
        if (_nncam == null) throw new Exception("取消失败");
        if (!_nncam.Trigger(0)) throw new Exception("取消失败");
    }

    #endregion

    #region 获取缓存

    public async Task<List<Picture>> GetCacheAsync()
    {
        var count = 30;
        if (_pictureList.Count == _target) return _pictureList;
        while (count > 0 && _pictureList.Count != _target)
        {
            await Task.Delay(100);
            count--;
        }

        return _pictureList;
    }

    #endregion

    #region 老化测试

    public void AgingTest()
    {
        Init();
        if (_nncam == null) throw new Exception("手动拍照失败");

        _flag = "aging";
        // 设置曝光时间
        if (!_nncam.put_ExpoTime(ExpoTime)) throw new Exception("设置白光曝光时间失败");
        // 触发拍摄
        if (!_nncam.Trigger(100)) throw new Exception("拍摄白光图失败");
    }

    #endregion

    #region 数据采集

    public async Task Collect(int start, int interval, int number)
    {
        Init();
        if (_nncam == null) throw new Exception("数据采集失败");

        _flag = "collect";
        _seq = 0;

        for (var i = 0; i < number; i++)
        {
            if (!_nncam.put_ExpoTime((uint)((start + interval * i) * 1000))) throw new Exception("设置曝光时间失败");
            // 触发拍摄
            if (!_nncam.Trigger(1)) throw new Exception("拍摄图片失败");

            await Task.Delay(start + interval * i + 100);
        }
    }

    #endregion

    #region 设置画质

    public void SetPixel(uint index)
    {
        Init();
        if (_nncam == null) return;
        if (_nncam.get_eSize(out var size))
        {
            if (size == index) return;
        }
        else
        {
            return;
        }

        _nncam?.Stop();
        _nncam?.put_eSize(index);

        if (SetCallBack()) return;
        _nncam?.Close();
        _nncam = null;
        throw new Exception("设置回调失败");
    }

    #endregion

    #region 获取温度

    public double GetTemperature()
    {
        if (_nncam == null || !_nncam.get_Temperature(out var nTemp)) return -100.0;

        return nTemp / 10.0;
    }

    #endregion

    #region 设置回调

    private bool SetCallBack()
    {
        if (_nncam == null) return false;
        return _nncam.StartPullModeWithCallback(evt =>
        {
            switch (evt)
            {
                case Nncam.eEVENT.EVENT_ERROR:
                    _nncam?.Close();
                    _nncam = null;
                    break;
                case Nncam.eEVENT.EVENT_DISCONNECTED:
                    _nncam?.Close();
                    _nncam = null;
                    break;
                case Nncam.eEVENT.EVENT_IMAGE:
                    OnEventImage();
                    break;
            }
        });
    }

    #endregion
    
    #region 获取图片

    private async void OnEventImage()
    {
        if (_nncam == null) return;
        if (!_nncam.get_ExpoTime(out var expo)) return;
        if (!_nncam.get_Size(out var width, out var height)) return;
        var buffer = Marshal.AllocHGlobal(width * height * 3);
        if (!_nncam.PullImageV3(buffer, 0, 24, 0, out var info)) return;
        // buffer => mat
        var mat = new Mat(height, width, MatType.CV_8UC3, buffer);
        
        // 序列
        _seq++;
        try
        {
            switch (_flag)
            {
                case "preview":
                {
                    _pictureList.Add(await SaveAsync(mat, info, (int)expo, -1));
                    // 关闭灯光
                    _serialPort.WritePort("Com2", DefaultProtocol.CloseLight().ToBytes());
                }
                    break;
                case "auto":
                {
                    switch (_seq)
                    {
                        // 暂存白光图
                        case 1:
                        {
                            // 保存图片
                            _pictureList.Add(await SaveAsync(mat, info, (int)expo));
                            // 关闭灯光
                            _serialPort.WritePort("Com2", DefaultProtocol.CloseLight().ToBytes());
                            _mat = mat.Clone();
                        }
                            break;
                        // 生成合成图
                        case 2:
                        {
                            // 保存图片
                            _pictureList.Add(await SaveAsync(mat, info, (int)expo, 1));
                            var combine = new Mat(height, width, MatType.CV_8UC3, new Scalar(0));
                            try
                            {
                                if (_mat != null) Cv2.Add(mat, _mat, combine);
                                _pictureList.Add(await SaveAsync(combine, info, (int)(expo + ExpoTime), 2));
                            }
                            finally
                            {
                                // 释放资源
                                combine.Dispose();
                            }
                        }
                            break;
                    }
                }
                    break;
                case "manual":
                {
                    switch (_seq)
                    {
                        case 1:
                            // 保存图片
                            _pictureList.Add(await SaveAsync(mat, info, (int)expo));
                            // 关闭灯光
                            _serialPort.WritePort("Com2", DefaultProtocol.CloseLight().ToBytes());
                            break;
                        case 2:
                            _mat = mat.Clone();
                            // 保存图片
                            _pictureList.Add(await SaveAsync(mat, info, (int)expo, 1));
                            break;
                        default:
                        {
                            var combine = new Mat(height, width, MatType.CV_8UC3, new Scalar(0));
                            try
                            {
                                if (_mat != null) Cv2.Add(mat, _mat, combine);
                                _mat = combine.Clone();
                                // 保存图片
                                _pictureList.Add(await SaveAsync(combine, info, (int)expo * (_seq - 1), 1));
                            }
                            finally
                            {
                                // 释放资源
                                combine.Dispose();
                            }
                        }
                            break;
                    }
                }
                    break;
                case "sampling":
                    _mat = mat.Clone();
                    break;
                case "collect":
                {
                    // 保存原图
                    var date = DateTime.Now.ToString("yyMMddHHmmssfff");
                    // 保存图片
                    var filePath = FileUtils.GetFileName(FileUtils.Collect, $"{date}.png");
                    var gray = new Mat();
                    Cv2.CvtColor(mat, gray, ColorConversionCodes.BGR2GRAY);
                    gray.SaveImage(filePath);
                }
                    break;
            }
        }
        finally
        {
            // 释放资源
            mat.Dispose();
            Marshal.FreeHGlobal(buffer);
        }
    }

    #endregion

    #region 保存图片

    private async Task<Picture> SaveAsync(
        Mat mat,
        Nncam.FrameInfoV3 info,
        int exposureTime,
        int type = 0
    )
    {
        #region 通用处理
        
        // 去除杂色
        mat.SetTo(new Scalar(0, 0, 0), mat.InRange(new Scalar(0, 0, 0), new Scalar(3, 3, 3)));
        // 中值滤波
        Cv2.MedianBlur(mat, mat, 5);
        // 校准图片
        var dst = Calibrate(mat);
        // 转换灰度图
        var gray = new Mat();
        Cv2.CvtColor(dst, gray, ColorConversionCodes.BGR2GRAY);
        // 直方图归一化
        Cv2.Normalize(gray, gray, 0, 255, NormTypes.MinMax, MatType.CV_8UC1);
        
        #endregion
        
        // 保存原图
        var date = DateTime.Now.ToString("yyMMddHHmmssfff");

        // 保存图片
        var filePath = FileUtils.GetFileName(type == -1 ? FileUtils.Preview : FileUtils.Exposure, $"{date}.png");
        
        gray.SaveImage(filePath);
        // 保存缩略图
        var thumb = new Mat();
        Cv2.Resize(gray, thumb, new Size(500, 500));
        var thumbnail = FileUtils.GetFileName(FileUtils.Thumbnail, $"{date}.jpg");
        thumb.SaveImage(thumbnail);

        // 释放资源
        gray.Dispose();
        thumb.Dispose();
        dst.Dispose();

        var picture = new Picture
        {
            UserId = _user.GetLogged()?.Id ?? 0,
            Name = type == -1 ? "Preview" : date,
            Path = filePath,
            Width = (int)info.width,
            Height = (int)info.height,
            Type = type,
            Thumbnail = thumbnail,
            ExposureTime = exposureTime,
            ExposureGain = info.expogain,
            BlackLevel = info.blacklevel,
            IsDelete = false,
            CreateTime = DateTime.Now,
            UpdateTime = DateTime.Now,
            DeleteTime = DateTime.Now
        };

        return type == -1 ? picture : await _picture.AddReturnModel(picture);
    }

    #endregion

    #region 计算曝光时间

    private static long CalculateExpo(Mat mat, nint expo, double snr)
    {
        // 计算信噪比
        Cv2.MeanStdDev(mat, out var mean, out var stddev);
        var snr1 = mean.Val0 / stddev.Val0;
        //计算曝光时间比例
        var ratio = Math.Pow(snr / snr1, 2);
        //计算目标曝光时间
        if (ratio != 0 && expo != 0) return (long)(expo / ratio);

        return 10000;
    }

    #endregion

    #region 相机标定

    private static Mat Calibrate(Mat src)
    {
        InputArray cameraMatrix;
        InputArray distCoeffs;

        switch (src)
        {
            // 3000 分辨率
            case { Width: 2992, Height: 3000 }:
                cameraMatrix = InputArray.Create(new[,]
                {
                    { 25084.866788553183, 0.0, 1490.4840423876637 }, { 0.0, 24953.07878266964, 1036.6913576658844 },
                    { 0.0, 0.0, 1.0 }
                });
                distCoeffs = InputArray.Create([
                    -6.714961926680041, 36.45951919419504, 0.11381577335005356, 0.012718469865032982,
                    -0.09751213606627142
                ]);
                break;
            // 1500 分辨率
            case { Width: 1488, Height: 1500 }:
                cameraMatrix = InputArray.Create(new[,]
                {
                    { 13043.982070571883, 0.0, 740.7185361307056 }, { 0.0, 12987.689563610029, 541.4143327555635 },
                    { 0.0, 0.0, 1.0 }
                });
                distCoeffs = InputArray.Create([
                    -7.19141774541563, 28.710192805407473, 0.10897004294135913, 0.013334599463637546,
                    0.29155982674105035
                ]);
                break;
            // 1000 分辨率
            case { Width: 992, Height: 998 }:
                cameraMatrix = InputArray.Create(new[,]
                {
                    { 10066.90980810934, 0.0, 356.1090956224924 }, { 0.0, 10104.216767255499, 472.4125568043425 },
                    { 0.0, 0.0, 1.0 }
                });
                distCoeffs = InputArray.Create([
                    -9.08285993829159, -112.07125081784204, 0.022485367380474335, 0.1431247144709765, -0.489913386337676
                ]);
                break;
            default:
                cameraMatrix = InputArray.Create(new[,]
                {
                    { 25084.866788553183, 0.0, 1490.4840423876637 }, { 0.0, 24953.07878266964, 1036.6913576658844 },
                    { 0.0, 0.0, 1.0 }
                });
                distCoeffs = InputArray.Create([
                    -6.714961926680041, 36.45951919419504, 0.11381577335005356, 0.012718469865032982,
                    -0.09751213606627142
                ]);
                break;
        }

        //根据相机内参和畸变参数矫正图片
        var dst = new Mat();

        var newCameraMatrix =
            Cv2.GetOptimalNewCameraMatrix(cameraMatrix, distCoeffs, src.Size(), 0, src.Size(), out var roi);
        // cameraMatrix 数组转换成 Mat 类型
        Cv2.Undistort(src, dst, cameraMatrix, distCoeffs, newCameraMatrix);
        // 裁剪图片并返回原始尺寸
        var res = new Mat();
        Cv2.Resize(dst[roi], res, src.Size());
        // 释放资源
        dst.Dispose();

        return res;
    }

    #endregion
}