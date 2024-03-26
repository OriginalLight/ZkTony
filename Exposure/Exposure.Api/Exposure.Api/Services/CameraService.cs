﻿using System.Runtime.InteropServices;
using Exposure.Api.Contracts.Services;
using Exposure.Api.Core.SerialPort.Default;
using Exposure.Api.Models;
using Exposure.Api.Utils;
using OpenCvSharp;

namespace Exposure.Api.Services;

public class CameraService(
    IOptionService option,
    ISerialPortService serialPort,
    IPictureService picture,
    IUserService user)
    : ICameraService
{
    private readonly List<Picture> _pictureList = [];
    private uint _expoTime = 1500;
    private string _flag = "preview";
    private Mat? _mat;
    private Nncam? _nncam;
    private int _seq;
    private int _target;

    #region 初始化

    public async Task InitAsync()
    {
        if (_nncam != null) return;

        try
        {
            // 从config中加载参数
            _expoTime = uint.Parse(await option.GetOptionValueAsync("ExpoTime") ?? "1500");
            var temperature = short.Parse(await option.GetOptionValueAsync("Temperature") ?? "-150");
            var gain = ushort.Parse(await option.GetOptionValueAsync("Gain") ?? "3000");

            var arr = Nncam.EnumV2();
            if (arr.Length == 0) throw new Exception("未找到设备");

            // 打开设备
            _nncam = Nncam.Open(arr[0].id);
            if (_nncam == null) throw new Exception("打开设备失败");

            // 设置参数
            if (!_nncam.put_Temperature(temperature)) throw new Exception("设置温度失败");
            if (!_nncam.put_Option(Nncam.eOPTION.OPTION_TRIGGER, 1)) throw new Exception("设置模式失败");
            if (!_nncam.put_AutoExpoEnable(false)) throw new Exception("参数自动曝光设置失败");
            if (!_nncam.put_Chrome(true)) throw new Exception("设置单色失败");
            if (!_nncam.put_ExpoAGain(gain)) throw new Exception("设置增益失败");

            // 设置回调
            if (!SetCallBack()) throw new Exception("设置回调失败");
        }
        catch (Exception)
        {
            _nncam?.Close();
            _nncam = null;
            serialPort.WritePort("Com1", DefaultProtocol.LedRed().ToBytes());
            serialPort.SetFlag("led", 5);
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
        await InitAsync();
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
            serialPort.WritePort("Com2", DefaultProtocol.OpenLight().ToBytes());
            // 延时100ms
            await Task.Delay(100);

            // 设置曝光时间
            if (!_nncam.put_ExpoTime(_expoTime)) throw new Exception("设置曝光时间失败");
            // 触发拍摄
            if (!_nncam.Trigger(1)) throw new Exception("预览失败");
        }
        catch (Exception)
        {
            serialPort.WritePort("Com2", DefaultProtocol.CloseLight().ToBytes());
            throw;
        }
    }

    #endregion

    #region 自动拍照

    public async Task<long> TakeAutoPhotoAsync(CancellationToken ctsToken)
    {
        await InitAsync();
        if (_nncam == null) throw new Exception("自动拍照失败");

        _mat = null;
        _flag = "sampling";
        var targetExpo = await CalculateExpo(0.1, ctsToken);
        if (targetExpo == 0) throw new Exception("计算曝光时间失败");

        _mat = null;
        _pictureList.Clear();
        _target = 3;
        _seq = 0;
        _flag = "auto";

        try
        {
            //打开灯光
            serialPort.WritePort("Com2", DefaultProtocol.OpenLight().ToBytes());
            // 延时100ms
            await Task.Delay(100, ctsToken);

            // 设置曝光时间
            if (!_nncam.put_ExpoTime(_expoTime)) throw new Exception("设置白光曝光时间失败");
            // 触发拍摄
            if (!_nncam.Trigger(1)) throw new Exception("拍摄白光图失败");
        }
        catch (Exception)
        {
            // 关闭灯光
            serialPort.WritePort("Com2", DefaultProtocol.CloseLight().ToBytes());
            throw;
        }

        // 延时500ms
        await Task.Delay(500, ctsToken);

        // 设置曝光时
        if (!_nncam.put_ExpoTime((uint)targetExpo)) throw new Exception("设置曝光图曝光时间失败");

        if (!_nncam.Trigger(1)) throw new Exception("拍摄曝光图失败");

        return targetExpo;
    }

    #endregion

    #region 手动拍照

    public async Task TakeManualPhotoAsync(int exposure, int frame, CancellationToken ctsToken)
    {
        await InitAsync();
        if (_nncam == null) throw new Exception("手动拍照失败");

        _mat = null;
        _pictureList.Clear();
        _target = frame + 1;
        _seq = 0;
        _flag = "manual";

        try
        {
            //打开灯光
            serialPort.WritePort("Com2", DefaultProtocol.OpenLight().ToBytes());
            // 延时100ms
            await Task.Delay(100, ctsToken);

            // 设置曝光时间
            if (!_nncam.put_ExpoTime(_expoTime)) throw new Exception("设置白光曝光时间失败");
            // 触发拍摄
            if (!_nncam.Trigger(1)) throw new Exception("拍摄白光图失败");
        }
        catch (Exception)
        {
            serialPort.WritePort("Com2", DefaultProtocol.CloseLight().ToBytes());
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

    public async Task CancelTask()
    {
        await InitAsync();
        if (_nncam == null) throw new Exception("取消失败");
        // 关闭灯光
        serialPort.WritePort("Com2", DefaultProtocol.CloseLight().ToBytes());
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

    public async Task AgingTest()
    {
        
        await InitAsync();
        if (_nncam == null) throw new Exception("手动拍照失败");
        // 设置flag
        _flag = "aging";
        // 设置曝光时间
        if (!_nncam.put_ExpoTime(5000000)) throw new Exception("设置曝光时间失败");
        // 触发拍摄
        if (!_nncam.Trigger(1)) throw new Exception("预览失败");
    }

    #endregion

    #region 数据采集

    public async Task Collect(int start, int interval, int number)
    {
        await InitAsync();
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

    public async Task SetPixel(uint index)
    {
        await InitAsync();
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
                    serialPort.WritePort("Com2", DefaultProtocol.CloseLight().ToBytes());
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
                            serialPort.WritePort("Com2", DefaultProtocol.CloseLight().ToBytes());
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
                                _pictureList.Add(await SaveAsync(combine, info, (int)(expo + _expoTime), 2));
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
                            serialPort.WritePort("Com2", DefaultProtocol.CloseLight().ToBytes());
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

        // 校准图片
        var dst = Calibrate(mat);
        // 转换灰度图
        var gray = new Mat();
        Cv2.CvtColor(dst, gray, ColorConversionCodes.BGR2GRAY);
        // 去除杂色
        mat.SetTo(0, mat.InRange(0, 3));
        // 中值滤波
        //Cv2.MedianBlur(gray, gray, 5);
        // 开运算
        Cv2.MorphologyEx(gray, gray, MorphTypes.Open, Cv2.GetStructuringElement(MorphShapes.Rect, new Size(3, 3)));
        // 直方图归一化
        gray.MinMaxLoc(out _, out double max);
        if (max > 10 && type == 1) Cv2.Normalize(gray, gray, 0, 255, NormTypes.MinMax, MatType.CV_8UC1);

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

        var picture1 = new Picture
        {
            UserId = user.GetLogged()?.Id ?? 0,
            Name = type == -1 ? "预览图" : date,
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

        return type == -1 ? picture1 : await picture.AddReturnModel(picture1);
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

    #region 计算曝光时间

    private async Task<long> CalculateExpo(double time, CancellationToken ctsToken)
    {
        if (_nncam == null) throw new Exception("计算曝光时间失败");
        // 设置曝光时间
        if (!_nncam.put_ExpoTime((uint)(time * 1000000))) throw new Exception("设置采样曝光时间失败");
        // 计算曝光时间
        if (!_nncam.Trigger(1)) throw new Exception("计算曝光时间失败");
        // 延时1300ms
        await Task.Delay((int)(time * 1000 + 1000), ctsToken);
        if (_mat == null) throw new Exception("获取采样图失败");
        var snr = CalculateSnr(_mat, time);
        _mat = null;

        if (Math.Abs(time - 0.1) < 0.1)
            return snr switch
            {
                <= -10 => GetScale(-50, -10, 1000000, 3000000, snr),
                > -10 => await CalculateExpo(1, ctsToken),
                _ => 1000000
            };

        if (Math.Abs(time - 1) < 0.1)
            return snr switch
            {
                <= -15 => GetScale(-50, -15, 2000000, 3000000, snr),
                <= 0 => GetScale(-15, 0, 3000000, 10000000, snr),
                <= 1 => GetScale(0, 1, 10000000, 14000000, snr),
                <= 1.5 => GetScale(1, 1.5, 14000000, 20000000, snr),
                <= 2 => GetScale(1.5, 2, 20000000, 30000000, snr),
                > 2 => await CalculateExpo(5, ctsToken),
                _ => 2000000
            };

        if (Math.Abs(time - 5) < 0.1) 
            return snr switch
            {
                <= 5 => GetScale(2, 5, 30000000, 40000000, snr),
                <= 6 => GetScale(5, 6, 40000000, 80000000, snr),
                <= 7 => GetScale(6, 7, 80000000, 140000000, snr),
                <= 7.5 => GetScale(7, 7.5, 140000000, 240000000, snr),
                <= 8 => GetScale(7.5, 8, 240000000, 300000000, snr),
                <= 10 => GetScale(8, 10, 300000000, 600000000, snr),
                > 10 => 600000000,
                _ => 30000000
            };

        return 0;
    }

    #endregion

    #region SNR

    private static double CalculateSnr(Mat image, double time)
    {
        var gray = new Mat();
        try
        {
            // 转换成灰度图
            Cv2.CvtColor(image, gray, ColorConversionCodes.BGR2GRAY);

            // 计算信噪比
            Cv2.MeanStdDev(gray, out var mean, out var stddev);

            // Calculate the signal power (square of the mean)
            var signalPower = Math.Pow(mean.Val0, 2);

            // Calculate the noise power (square of the standard deviation)
            var noisePower = Math.Pow(stddev.Val0, 2);

            // Adjust the noise power based on the exposure time
            var adjustedNoisePower = noisePower / time;

            // Calculate and return the adjusted SNR value
            return 10 * Math.Log10(signalPower / adjustedNoisePower);
        }
        finally
        {
            // 释放资源
            gray.Dispose();
        }
    }

    #endregion

    #region 比例缩放
    
    private static long GetScale(double min, double max, double minTarget, double maxTarget, double value)
    {
        return (long)((maxTarget - minTarget) * (value - min) / (max - min) + minTarget);
    }

    #endregion
}