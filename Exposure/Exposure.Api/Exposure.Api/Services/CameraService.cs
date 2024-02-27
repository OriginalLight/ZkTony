using System.Runtime.InteropServices;
using Exposure.Api.Contracts.Services;
using Exposure.Api.Core.SerialPort.Default;
using Exposure.Api.Models;
using OpenCvSharp;

namespace Exposure.Api.Services;

public class CameraService : ICameraService
{
    private readonly List<Picture> _pictureList = [];
    private readonly ISerialPortService _serialPort;
    private readonly IPictureService _picture;
    private readonly IUserService _user;
    private readonly uint _expoTime = 15000;
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

    public void Initialize()
    {
        if (_nncam != null) return;

        var arr = Nncam.EnumV2();
        if (arr.Length <= 0) throw new Exception("未找到设备");

        // 打开设备
        _nncam = Nncam.Open(arr[0].id);
        if (_nncam == null) throw new Exception("打开设备失败");

        // 设置参数
        if (!_nncam.put_Option(Nncam.eOPTION.OPTION_TRIGGER, 1)) throw new Exception("设置模式失败");

        if (!_nncam.put_AutoExpoEnable(false)) throw new Exception("参数自动曝光设置失败");

        // 设置回调
        if (!SetCallBack())
        {
            _nncam?.Close();
            _nncam = null;
            throw new Exception("设置回调失败");
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

    public async void PreviewAsync()
    {
        Initialize();
        if (_nncam == null) throw new Exception("预览失败");
        // 清空队列
        _pictureList.Clear();
        // 设置flag
        _flag = "preview";
        // 目标张数
        _target = 1;

        //打开灯光
        _serialPort.WritePort("Com2", DefaultProtocol.OpenLight().ToBytes());
        // 延时100ms
        await Task.Delay(100);

        // 设置曝光时间
        if (!_nncam.put_ExpoTime(_expoTime)) throw new Exception("设置曝光时间失败");
        // 触发拍摄
        if (!_nncam.Trigger(1)) throw new Exception("预览失败");
    }

    #endregion

    #region 自动拍照

    public async Task<long> TakeAutoPhotoAsync(CancellationToken ctsToken)
    {
        Initialize();
        if (_nncam == null) return 0;
        
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
        var mat = _mat.Clone();
        // 转换成灰度图
        Cv2.CvtColor(mat, mat, ColorConversionCodes.BGR2GRAY);
        var expo = CalculateExpo(mat, (int)targetExpo, 0.5);
        if (expo > 1000000L * 60L * 60L) throw new Exception("曝光时间过长");
        targetExpo = Math.Max((uint)expo, 10000);
        
        _mat = null;
        _pictureList.Clear();
        _target = 3;
        _seq = 0;
        _flag = "auto";
        
        //打开灯光
        _serialPort.WritePort("Com2", DefaultProtocol.OpenLight().ToBytes());
        // 延时100ms
        await Task.Delay(100, ctsToken);
        
        // 设置曝光时间
        if (!_nncam.put_ExpoTime(_expoTime)) throw new Exception("设置白光曝光时间失败");
        // 触发拍摄
        if (!_nncam.Trigger(1)) throw new Exception("拍摄白光图失败");

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
        Initialize();
        if (_nncam == null) return;

        _mat = null;
        _pictureList.Clear();
        _target = frame + 1;
        _seq = 0;
        _flag = "manual";
        
        //打开灯光
        _serialPort.WritePort("Com2", DefaultProtocol.OpenLight().ToBytes());
        // 延时100ms
        await Task.Delay(100, ctsToken);
        
        // 设置曝光时间
        if (!_nncam.put_ExpoTime(_expoTime)) throw new Exception("设置白光曝光时间失败");
        // 触发拍摄
        if (!_nncam.Trigger(1)) throw new Exception("拍摄白光图失败");

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
        Initialize();
        if (!(_nncam != null && _nncam.Trigger(0))) throw new Exception("取消失败");
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

    #region 设置画质

    public void SetPixel(uint index)
    {
        Initialize();
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
                    OnEventError();
                    break;
                case Nncam.eEVENT.EVENT_DISCONNECTED:
                    OnEventDisconnected();
                    break;
                case Nncam.eEVENT.EVENT_IMAGE:
                    OnEventImage();
                    break;
            }
        });
    }

    #endregion

    #region 设备错误

    private void OnEventError()
    {
        _nncam?.Close();
        _nncam = null;
    }

    #endregion

    #region 断开连接

    private void OnEventDisconnected()
    {
        _nncam?.Close();
        _nncam = null;
    }

    #endregion

    #region 获取图片

    private async void OnEventImage()
    {
        if (_nncam == null) return;
        if (!_nncam.get_ExpoTime(out var expoTime)) return;
        if (!_nncam.get_Size(out var width, out var height)) return;
        var buffer = Marshal.AllocHGlobal(width * height * 3);
        if (!_nncam.PullImageV3(buffer, 0, 24, 0, out var info)) return;
        var mat = new Mat(height, width, MatType.CV_8UC3, buffer);
        _seq++;
        try
        {
            switch (_flag)
            {
                case "preview":
                {
                    _pictureList.Add(await SaveAsync(mat, info, (int)expoTime, -1));
                    // 关闭灯光
                    _serialPort.WritePort("Com2", DefaultProtocol.CloseLight().ToBytes());
                }
                    break;
                case "auto":
                {
                    // 保存图片
                    _pictureList.Add(await SaveAsync(mat, info, (int)expoTime, _seq - 1, _seq - 1));
                    switch (_seq)
                    {
                        // 暂存白光图
                        case 1:
                        {
                            // 关闭灯光
                            _serialPort.WritePort("Com2", DefaultProtocol.CloseLight().ToBytes());
                            _mat = mat.Clone();
                        }
                            break;
                        // 生成合成图
                        case 2:
                        {
                            var combine = new Mat(height, width, MatType.CV_8UC3, new Scalar(0));
                            try
                            {
                                if (_mat != null) Cv2.Add(mat, _mat, combine);
                                _pictureList.Add(await SaveAsync(combine, info, (int)(expoTime + _expoTime), 2, _seq));
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
                            _pictureList.Add(await SaveAsync(mat, info, (int)expoTime));
                            // 关闭灯光
                            _serialPort.WritePort("Com2", DefaultProtocol.CloseLight().ToBytes());
                            break;
                        case 2:
                            _pictureList.Add(await SaveAsync(mat, info, (int)expoTime, 1, 1));
                            _mat = mat.Clone();
                            break;
                        default:
                        {
                            var combine = new Mat(height, width, MatType.CV_8UC3, new Scalar(0));
                            try
                            {
                                if (_mat != null) Cv2.Add(mat, _mat, combine);
                                _mat = combine.Clone();
                                _pictureList.Add(await SaveAsync(combine, info, (int)expoTime * (_seq - 1), 1, _seq - 1));
                            }
                            finally
                            {
                                // 释放资源
                                combine.Dispose();
                            }
                        } break;
                    }
                }
                    break;
                case "sampling":
                    _mat = mat.Clone();
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
        int type = 0,
        int offset = 0
    )
    {
        var cali = Calibrate(mat);
        var tmp = cali.Clone();
        // 保存原图
        var myPictures = Environment.GetFolderPath(Environment.SpecialFolder.MyPictures);
        var date = DateTime.Now.AddSeconds(offset).ToString("yyyyMMddHHmmss");

        var savePath = Path.Combine(myPictures, type == -1 ? "Preview" : "Exposure");
        if (!Directory.Exists(savePath)) Directory.CreateDirectory(savePath);
        var filePath = Path.Combine(savePath, $"{date}.png");
        Cv2.CvtColor(tmp, tmp, ColorConversionCodes.BGR2GRAY);
        tmp.SaveImage(filePath);

        // 保存缩略图
        var thumbnail = new Mat();
        Cv2.Resize(cali, thumbnail, new Size(500, 500));
        var thumbnailPath = Path.Combine(myPictures, "Thumbnail");
        if (!Directory.Exists(thumbnailPath)) Directory.CreateDirectory(thumbnailPath);
        var thumbnailFilePath = Path.Combine(thumbnailPath, $"{date}.jpg");
        Cv2.CvtColor(thumbnail, thumbnail, ColorConversionCodes.BGR2GRAY);
        thumbnail.SaveImage(thumbnailFilePath);

        cali.Dispose();
        tmp.Dispose();
        thumbnail.Dispose();

        var pic = new Picture
        {
            UserId = _user.GetLogged()?.Id ?? 0,
            Name = type == -1 ? "Preview" : date,
            Path = filePath,
            Width = (int)info.width,
            Height = (int)info.height,
            Type = type,
            Thumbnail = thumbnailFilePath,
            ExposureTime = exposureTime,
            ExposureGain = info.expogain,
            BlackLevel = info.blacklevel,
            IsDelete = false,
            CreateTime = DateTime.Now,
            UpdateTime = DateTime.Now,
            DeleteTime = DateTime.Now
        };

        if (type == -1)
        {
            return pic;
        }

        return await _picture.AddReturnModel(pic);
    }

    #endregion

    #region 计算曝光时间

    private long CalculateExpo(Mat mat, nint expo, double snr)
    {
        // 计算信噪比
        Cv2.MeanStdDev(mat, out var mean, out var stddev);
        var snr1 = mean.Val0 / stddev.Val0;
        //计算曝光时间比例
        var ratio = Math.Pow(snr / snr1, 2);
        //计算目标曝光时间
        if (ratio != 0 && expo != 0)
        {
            return (long)(expo / ratio);
        }
        
        return 10000;
    }

    #endregion

    #region 相机标定

    private Mat Calibrate(Mat src)
    {
        InputArray cameraMatrix;
        InputArray distCoeffs;

        switch (src)
        {
            // 3000 分辨率
            case { Width: 2992, Height: 3000 }:
                cameraMatrix = InputArray.Create(new[,]
                {
                    { 118314.15287558122, 0.0, 1503.13160733239 }, { 0.0, 116818.79737632291, 1497.8070325395977 },
                    { 0.0, 0.0, 1.0 }
                });
                distCoeffs = InputArray.Create([
                    -137.90549974890038, -0.8186532104734332, 0.05846521423784008, 0.05466066925233416,
                    -0.00016929702742385364
                ]);
                break;
            // 1500 分辨率
            case { Width: 1488, Height: 1500 }:
                cameraMatrix = InputArray.Create(new[,]
                {
                    { 52556.42669267867, 0.0, 747.0623164164573 }, { 0.0, 51969.13299781103, 748.561870899801 },
                    { 0.0, 0.0, 1.0 }
                });
                distCoeffs = InputArray.Create([
                    -107.59893476651766, -0.2684743385724511, 0.05532072109809101, 0.05249425464476222,
                    -6.902150131244834
                ]);
                break;
            // 1000 分辨率
            case { Width: 992, Height: 998 }:
                cameraMatrix = InputArray.Create(new[,]
                {
                    { 33532.96664102788, 0.0, 497.88846297434503 }, { 0.0, 33164.49293883739, 497.9662889279552 },
                    { 0.0, 0.0, 1.0 }
                });
                distCoeffs = InputArray.Create([
                    -98.94473239189564, -0.14600407375804034, 0.05515329136584197, 0.04288691934394333,
                    -4.015667889398248
                ]);
                break;
            default:
                cameraMatrix = InputArray.Create(new[,]
                {
                    { 118314.15287558122, 0.0, 1503.13160733239 }, { 0.0, 116818.79737632291, 1497.8070325395977 },
                    { 0.0, 0.0, 1.0 }
                });
                distCoeffs = InputArray.Create([
                    -137.90549974890038, -0.8186532104734332, 0.05846521423784008, 0.05466066925233416,
                    -0.00016929702742385364
                ]);
                break;
        }

        //根据相机内参和畸变参数矫正图片
        var dst = new Mat();
        var newCameraMatrix =
            Cv2.GetOptimalNewCameraMatrix(cameraMatrix, distCoeffs, src.Size(), 1, src.Size(), out var roi);
        // cameraMatrix 数组转换成 Mat 类型
        Cv2.Undistort(src, dst, cameraMatrix, distCoeffs, newCameraMatrix);
        // 裁剪图片
        return dst[roi];
    }

    #endregion
}