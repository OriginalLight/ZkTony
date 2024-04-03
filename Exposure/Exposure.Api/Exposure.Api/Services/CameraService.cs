using System.Runtime.InteropServices;
using Exposure.Api.Contracts.Services;
using Exposure.Api.Core.SerialPort.Default;
using Exposure.Api.Models;
using Exposure.Api.Utils;
using Microsoft.Extensions.Localization;
using OpenCvSharp;

namespace Exposure.Api.Services;

public class CameraService(
    IOptionService option,
    ISerialPortService serialPort,
    IPictureService picture,
    IUserService user,
    IStringLocalizer<SharedResources> localizer) : ICameraService
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
            if (arr.Length == 0) throw new Exception(localizer.GetString("Error0001").Value);

            // 打开设备
            _nncam = Nncam.Open(arr[0].id);
            if (_nncam == null) throw new Exception(localizer.GetString("Error0002").Value);

            // 设置参数
            if (!_nncam.put_Temperature(temperature)) throw new Exception(localizer.GetString("Error0003").Value);
            if (!_nncam.put_Option(Nncam.eOPTION.OPTION_TRIGGER, 1))
                throw new Exception(localizer.GetString("Error0004").Value);
            if (!_nncam.put_AutoExpoEnable(false)) throw new Exception(localizer.GetString("Error0005").Value);
            if (!_nncam.put_Chrome(true)) throw new Exception(localizer.GetString("Error0006").Value);
            if (!_nncam.put_ExpoAGain(gain)) throw new Exception(localizer.GetString("Error0007").Value);

            // 设置回调
            if (!SetCallBack()) throw new Exception(localizer.GetString("Error0008").Value);
        }
        catch (Exception)
        {
            _nncam?.Close();
            _nncam = null;
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
        if (_nncam == null)
            throw new Exception($"{localizer.GetString("Preview")}{localizer.GetString("Failure").Value}");
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
            if (!_nncam.put_ExpoTime(_expoTime)) throw new Exception(localizer.GetString("Error0009").Value);
            // 触发拍摄
            if (!_nncam.Trigger(1))
                throw new Exception($"{localizer.GetString("Preview")}{localizer.GetString("Failure").Value}");
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
        if (_nncam == null) throw new Exception(localizer.GetString("Error0011").Value);

        _mat = null;
        _flag = "sampling";
        var targetExpo = await CalculateExpo(0.1, ctsToken);
        // 验证曝光时间
        if (targetExpo == 0) targetExpo = 1000;

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
            if (!_nncam.put_ExpoTime(_expoTime)) throw new Exception(localizer.GetString("Error0009").Value);
            // 触发拍摄
            if (!_nncam.Trigger(1)) throw new Exception(localizer.GetString("Error0010").Value);
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
        if (!_nncam.put_ExpoTime((uint)targetExpo)) throw new Exception(localizer.GetString("Error0009").Value);

        if (!_nncam.Trigger(1)) throw new Exception(localizer.GetString("Error0010").Value);

        return targetExpo;
    }

    #endregion

    #region 手动拍照

    public async Task TakeManualPhotoAsync(int exposure, int frame, CancellationToken ctsToken)
    {
        await InitAsync();
        if (_nncam == null) throw new Exception(localizer.GetString("Error0011").Value);

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
            if (!_nncam.put_ExpoTime(_expoTime)) throw new Exception(localizer.GetString("Error0009").Value);
            // 触发拍摄
            if (!_nncam.Trigger(1)) throw new Exception(localizer.GetString("Error0010").Value);
        }
        catch (Exception)
        {
            serialPort.WritePort("Com2", DefaultProtocol.CloseLight().ToBytes());
            throw;
        }

        // 延时500ms
        await Task.Delay(500, ctsToken);

        // 设置曝光时
        if (!_nncam.put_ExpoTime((uint)(exposure / frame))) throw new Exception(localizer.GetString("Error0009").Value);
        // 触发拍摄
        if (!_nncam.Trigger((ushort)frame)) throw new Exception(localizer.GetString("Error0010").Value);
    }

    #endregion

    #region 取消拍照

    public async Task CancelTask()
    {
        await InitAsync();
        if (_nncam == null) throw new Exception(localizer.GetString("Error0011").Value);
        // 关闭灯光
        serialPort.WritePort("Com2", DefaultProtocol.CloseLight().ToBytes());
        if (!_nncam.Trigger(0)) throw new Exception(localizer.GetString("Error0010").Value);
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
        if (_nncam == null) throw new Exception(localizer.GetString("Error0011").Value);
        // 设置flag
        _flag = "aging";
        // 设置曝光时间
        if (!_nncam.put_ExpoTime(5000000)) throw new Exception(localizer.GetString("Error0009").Value);
        // 触发拍摄
        if (!_nncam.Trigger(1)) throw new Exception(localizer.GetString("Error0010").Value);
    }

    #endregion

    #region 数据采集

    public async Task Collect(int start, int interval, int number)
    {
        await InitAsync();
        if (_nncam == null) throw new Exception(localizer.GetString("Error0011").Value);

        _flag = "collect";
        _seq = 0;

        for (var i = 0; i < number; i++)
        {
            if (!_nncam.put_ExpoTime((uint)((start + interval * i) * 1000)))
                throw new Exception(localizer.GetString("Error0009").Value);
            // 触发拍摄
            if (!_nncam.Trigger(1)) throw new Exception(localizer.GetString("Error0010").Value);

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
        throw new Exception(localizer.GetString("Error0008").Value);
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
                            if (_mat != null)
                            {
                                // _mat 黑白反色处理
                                Cv2.BitwiseNot(mat, mat);
                                // 正片叠底将mat叠在-mat上面
                                var multiply = OpenCvUtils.Multiply(_mat, mat);
                                // 保存图片
                                _pictureList.Add(await SaveAsync(multiply, info, (int)(expo + _expoTime), 2));
                                // 释放资源
                                multiply.Dispose();
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
        var dst = OpenCvUtils.Calibrate(mat);
        // 转换灰度图
        var gray = new Mat();
        Cv2.CvtColor(dst, gray, ColorConversionCodes.BGR2GRAY);
        // 去除杂色
        gray.SetTo(0, gray.InRange(0, 3));
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
            Name = type == -1 ? localizer.GetString("Preview").Value : date,
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

    #region 计算曝光时间

    private async Task<long> CalculateExpo(double time, CancellationToken ctsToken)
    {
        if (_nncam == null) throw new Exception(localizer.GetString("Error0011").Value);
        // 设置曝光时间
        if (!_nncam.put_ExpoTime((uint)(time * 1000000))) throw new Exception(localizer.GetString("Error0009").Value);
        // 计算曝光时间
        if (!_nncam.Trigger(1)) throw new Exception(localizer.GetString("Error0010").Value);
        // 延时1300ms
        await Task.Delay((int)(time * 1000 + 1000), ctsToken);
        // 获取图片
        if (_mat == null) throw new Exception(localizer.GetString("Error0011").Value);
        // 计算信噪比
        var snr = OpenCvUtils.CalculateSnr(_mat, time);
        // 计算白色区域占比
        var percentage = OpenCvUtils.CalculatePercentage(_mat, 10, 255);
        // 清空_mat
        _mat = null;

        if (Math.Abs(time - 0.1) < 0.1)
            return percentage switch
            {
                <= 0.05 => snr switch
                {
                    <= -10 => GetScale(-50, -10, 1000000, 3000000, snr),
                    > -10 => await CalculateExpo(1, ctsToken),
                    _ => 1000000
                },
                <= 0.1 => GetScale(0.05, 0.1, 5000000, 10000000, percentage),
                <= 0.5 => GetScale(0.1, 0.5, 2000000, 5000000, percentage),
                _ => GetScale(0.5, 1, 100000, 2000000, percentage)
            };


        if (Math.Abs(time - 1) < 0.1)
            return percentage switch
            {
                <= 0.05 => snr switch
                {
                    <= -15 => GetScale(-50, -15, 2000000, 3000000, snr),
                    <= 0 => GetScale(-15, 0, 3000000, 10000000, snr),
                    <= 1 => GetScale(0, 1, 10000000, 14000000, snr),
                    <= 1.5 => GetScale(1, 1.5, 14000000, 20000000, snr),
                    <= 2 => GetScale(1.5, 2, 20000000, 30000000, snr),
                    > 2 => await CalculateExpo(5, ctsToken),
                    _ => 2000000
                },
                <= 0.1 => GetScale(0.05, 0.1, 5000000, 10000000, percentage),
                <= 0.5 => GetScale(0.1, 0.5, 2000000, 5000000, percentage),
                _ => GetScale(0.5, 1, 100000, 2000000, percentage)
            };

        if (Math.Abs(time - 5) < 0.1)
            return percentage switch
            {
                <= 0.05 => snr switch
                {
                    <= -20 => GetScale(-50, -20, 3000000, 5000000, snr),
                    <= 0 => GetScale(-20, 0, 5000000, 10000000, snr),
                    <= 1 => GetScale(0, 1, 10000000, 20000000, snr),
                    <= 2 => GetScale(1, 2, 20000000, 30000000, snr),
                    <= 3 => GetScale(2, 3, 30000000, 40000000, snr),
                    <= 4 => GetScale(3, 4, 40000000, 50000000, snr),
                    <= 5 => GetScale(4, 5, 50000000, 60000000, snr),
                    > 5 => 60000000,
                    _ => 3000000
                },
                <= 0.1 => GetScale(0.05, 0.1, 15000000, 30000000, percentage),
                <= 0.5 => GetScale(0.1, 0.5, 6000000, 15000000, percentage),
                _ => GetScale(0.5, 1, 300000, 6000000, percentage)
            };

        return 0;
    }

    #endregion
    

    #region 比例缩放

    private static long GetScale(double min, double max, double minTarget, double maxTarget, double value)
    {
        return (long)((maxTarget - minTarget) * (value - min) / (max - min) + minTarget);
    }
    
    #endregion
    
}