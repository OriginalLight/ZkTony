using Exposure.Api.Contracts.Services;
using Exposure.Api.Models;
using Exposure.Protocal.Default;
using Exposure.Utilities;
using Microsoft.Extensions.Localization;
using Newtonsoft.Json;
using OpenCvSharp;
using Serilog;
using System.Runtime.InteropServices;
using Exposure.Api.Models.Dto;
using Size = OpenCvSharp.Size;

namespace Exposure.Api.Services;

public class CameraService(
    IOptionService option,
    ISerialPortService serialPort,
    IPhotoService photo,
    IAlbumService album,
    IUserService user,
    IStringLocalizer<SharedResources> localizer) : ICameraService
{
    private readonly List<Photo> _photoList = [];
    private string _flag = "none";
    private Mat? _mat;
    private Nncam? _nncam;
    private int _seq;
    private int _target;
    private Album? _album;

    #region 相机

    public Nncam? Camera => _nncam;

    #endregion

    #region 初始化

    public async Task InitAsync()
    {
        if (_nncam != null) return;

        try
        {
            // 从config中加载参数
            var temperature = short.Parse(await option.GetOptionValueAsync("Temperature") ?? "-150");
            var gain = ushort.Parse(await option.GetOptionValueAsync("Gain") ?? "500");

            var arr = Nncam.EnumV2();
            if (arr.Length == 0) throw new Exception(localizer.GetString("Error0001").Value);

            Log.Information("相机数量：" + arr.Length);

            // 打开设备
            _nncam = Nncam.Open(arr[0].id);
            if (_nncam == null) throw new Exception(localizer.GetString("Error0002").Value);

            Log.Information("打开相机：" + arr[0].displayname);

            // 设置温度
            if (_nncam.put_Temperature(temperature))
                Log.Information("设置温度：" + temperature);
            else
                throw new Exception(localizer.GetString("Error0003").Value);

            // 设置触发模式
            if (_nncam.put_Option(Nncam.eOPTION.OPTION_TRIGGER, 1))
                Log.Information("设置触发模式");
            else
                throw new Exception(localizer.GetString("Error0004").Value);

            // 设置RGB48
            if (_nncam.put_Option(Nncam.eOPTION.OPTION_RGB, 1))
                Log.Information("设置RGB48");
            else
                throw new Exception(localizer.GetString("Error0004").Value);

            // 设置转换增益
            if (_nncam.put_Option(Nncam.eOPTION.OPTION_CG, 1))
                Log.Information("设置转换增益 HCG");
            else
                throw new Exception(localizer.GetString("Error0004").Value);

            // 设置位深度
            if (_nncam.put_Option(Nncam.eOPTION.OPTION_BITDEPTH, 1))
                Log.Information("设置位深度 14");
            else
                throw new Exception(localizer.GetString("Error0004").Value);

            // 设置自动曝光
            if (_nncam.put_AutoExpoEnable(false))
                Log.Information("关闭自动曝光");
            else
                throw new Exception(localizer.GetString("Error0005").Value);
            // 设置彩色
            if (_nncam.put_Chrome(true))
                Log.Information("设置单色模式");
            else
                throw new Exception(localizer.GetString("Error0006").Value);
            // 设置增益
            if (_nncam.put_ExpoAGain(gain))
                Log.Information("设置增益：" + gain);
            else
                throw new Exception(localizer.GetString("Error0007").Value);

            // 设置回调
            if (SetCallBack())
                Log.Information("设置回调");
            else
                throw new Exception(localizer.GetString("Error0008").Value);

            Log.Information("相机初始化成功！");
        }
        catch (Exception e)
        {
            _nncam?.Close();
            _nncam = null;
            Log.Error(e, "相机初始化错误！");
            throw;
        }
    }

    #endregion

    #region 停止

    public void Stop()
    {
        _nncam?.Close();
        _nncam = null;
        Log.Information("相机停止");
    }

    #endregion

    #region 预览

    public async Task<Photo> PreviewAsync()
    {
        await InitAsync();
        if (_nncam == null)
            throw new Exception($"{localizer.GetString("Preview")}{localizer.GetString("Failure").Value}");
        // 清空队列
        _photoList.Clear();
        // 设置flag
        _flag = "preview";
        // 目标张数
        _target = 1;
        // 序列
        _seq = 0;

        try
        {
            Log.Information("开始预览");
            //打开灯光
            serialPort.WritePort("Com2", DefaultProtocol.OpenLight().ToBytes());
            // 延时100ms
            await Task.Delay(100);

            // 设置曝光时间
            var expoTime = uint.Parse(await option.GetOptionValueAsync("ExpoTime") ?? "30000");
            if (_nncam.put_ExpoTime(expoTime))
                Log.Information("设置曝光时间：" + expoTime);
            else
                throw new Exception(localizer.GetString("Error0009").Value);
            // 触发拍摄
            if (_nncam.Trigger(1))
                Log.Information("触发拍摄");
            else
                throw new Exception(localizer.GetString("Error0010").Value);

            var flag = 30;
            while (_photoList.Count != _target && flag > 0)
            {
                await Task.Delay(100);
                flag--;
            }

            if (_photoList.Count == 0) throw new Exception(localizer.GetString("Error0017").Value);

            return _photoList[0];
        }
        catch (Exception e)
        {
            serialPort.WritePort("Com2", DefaultProtocol.CloseLight().ToBytes());
            Log.Error(e, "预览失败！");
            throw;
        }
    }

    #endregion

    #region 自动拍照

    public async Task<long> TakeAutoPhotoAsync(CancellationToken ctsToken)
    {
        await InitAsync();
        if (_nncam == null) throw new Exception(localizer.GetString("Error0011").Value);

        _seq = 0;
        _flag = "sampling";

        var targetExpoTime = await ExpoWithThreshold(ctsToken);

        _mat?.Dispose();
        _mat = null;
        _album = await album.AddReturnModel(new Album
        {
            UserId = user.GetLogged()?.Id ?? 0,
            Name = DateTime.Now.ToString("yyMMddHHmmssfff")
        });
        _photoList.Clear();
        _target = 3;
        _seq = 0;
        _flag = "auto";

        try
        {
            Log.Information("开始自动拍照");
            //打开灯光
            serialPort.WritePort("Com2", DefaultProtocol.OpenLight().ToBytes());
            // 延时100ms
            await Task.Delay(100, ctsToken);

            // 设置曝光时间
            var expoTime = uint.Parse(await option.GetOptionValueAsync("ExpoTime") ?? "30000");
            if (_nncam.put_ExpoTime(expoTime))
                Log.Information("设置曝光时间：" + expoTime);
            else
                throw new Exception(localizer.GetString("Error0009").Value);
            // 触发拍摄
            if (_nncam.Trigger(1))
                Log.Information("触发拍摄");
            else
                throw new Exception(localizer.GetString("Error0010").Value);

            // 延时
            await Task.Delay(2000, ctsToken);
        }
        catch (Exception e)
        {
            // 关闭灯光
            serialPort.WritePort("Com2", DefaultProtocol.CloseLight().ToBytes());
            Log.Error(e, "自动拍照失败！");
            throw;
        }

        // 设置曝光时
        if (_nncam.put_ExpoTime((uint)targetExpoTime))
            Log.Information("设置曝光时间：" + targetExpoTime);
        else
            throw new Exception(localizer.GetString("Error0009").Value);

        if (_nncam.Trigger(1))
            Log.Information("触发拍摄");
        else
            throw new Exception(localizer.GetString("Error0010").Value);

        Log.Information("自动拍照成功！");

        return targetExpoTime;
    }

    #endregion

    #region 手动拍照

    public async Task TakeManualPhotoAsync(uint exposure, int frame, CancellationToken ctsToken)
    {
        await InitAsync();
        if (_nncam == null) throw new Exception(localizer.GetString("Error0011").Value);

        _mat?.Dispose();
        _mat = null;
        _album = await album.AddReturnModel(new Album
        {
            UserId = user.GetLogged()?.Id ?? 0,
            Name = DateTime.Now.ToString("yyMMddHHmmssfff")
        });
        _photoList.Clear();
        _target = frame + 1;
        _seq = 0;
        _flag = "manual";

        try
        {
            Log.Information("开始手动拍照");
            //打开灯光
            serialPort.WritePort("Com2", DefaultProtocol.OpenLight().ToBytes());
            // 延时100ms
            await Task.Delay(100, ctsToken);

            // 设置曝光时间
            var expoTime = uint.Parse(await option.GetOptionValueAsync("ExpoTime") ?? "30000");
            if (_nncam.put_ExpoTime(expoTime))
                Log.Information("设置曝光时间：" + expoTime);
            else
                throw new Exception(localizer.GetString("Error0009").Value);
            // 触发拍摄
            if (_nncam.Trigger(1))
                Log.Information("触发拍摄");
            else
                throw new Exception(localizer.GetString("Error0010").Value);

            // 延时
            await Task.Delay(2000, ctsToken);
        }
        catch (Exception e)
        {
            // 关闭灯光
            serialPort.WritePort("Com2", DefaultProtocol.CloseLight().ToBytes());
            Log.Error(e, "手动拍照失败！");
            throw;
        }


        // 设置曝光时
        if (_nncam.put_ExpoTime((uint)(exposure / frame)))
            Log.Information("设置曝光时间：" + (int)(exposure / frame));
        else
            throw new Exception(localizer.GetString("Error0009").Value);

        if (_nncam.Trigger((ushort)frame))
            Log.Information("触发拍摄：" + frame);
        else
            throw new Exception(localizer.GetString("Error0010").Value);

        Log.Information("手动拍照成功！");
    }

    #endregion

    #region 取消拍照

    public async Task CancelTask()
    {
        await InitAsync();
        if (_nncam == null) throw new Exception(localizer.GetString("Error0011").Value);
        // 关闭灯光
        serialPort.WritePort("Com2", DefaultProtocol.CloseLight().ToBytes());

        if (_album != null)
        {
            await album.DeleteByIds([_album.Id]);
        }

        if (!_nncam.Trigger(0)) throw new Exception(localizer.GetString("Error0010").Value);

        _photoList.Clear();
        _mat?.Dispose();
        _mat = null;
        _seq = 0;
        _album = null;
        Log.Information("取消拍照任务");
    }

    #endregion

    #region 获取缓存

    public async Task<AlbumOutDto?> GetCacheAsync()
    {
        if (_album == null) return null;
        if (_photoList.Count == _target)
        {
            Log.Information("获取图集：" + _album.Id);
            return await album.GetById(_album.Id);
        }

        var count = 50;
        while (count > 0 && _photoList.Count != _target)
        {
            await Task.Delay(100);
            count--;
        }

        Log.Information("获取图集：" + _album.Id);
        return await album.GetById(_album.Id);
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
        if (!_nncam.put_ExpoTime(1_000_000)) throw new Exception(localizer.GetString("Error0009").Value);
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

    #region 丢图测试

    public async Task LostTest(int number)
    {
        await InitAsync();
        if (_nncam == null) throw new Exception(localizer.GetString("Error0011").Value);

        _flag = "lost";
        _seq = 0;
        _mat?.Dispose();
        _mat = null;

        for (var i = 0; i < number; i++)
        {
            // 随机曝光时间
            var expoTime = new Random().Next(1, 3000) * 1000;
            if (!_nncam.put_ExpoTime((uint)expoTime))
                throw new Exception(localizer.GetString("Error0009").Value);
            // 触发拍摄
            if (!_nncam.Trigger(1)) throw new Exception(localizer.GetString("Error0010").Value);
            await Task.Delay(expoTime / 1000 + 500);
            if (_mat == null)
            {
                throw new Exception("Lost Photo At " + i);
            }
            _mat.Dispose();
            _mat = null;
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

        _nncam.Stop();
        _nncam.put_eSize(index);

        if (SetCallBack())
        {
            Log.Information("设置画质成功：" + index);
            return;
        }

        _nncam.Close();
        _nncam = null;
        Log.Error("设置画质失败：" + index);
        throw new Exception(localizer.GetString("Error0008").Value);
    }

    #endregion

    #region 获取温度

    public double GetTemperature()
    {
        if (_nncam == null || !_nncam.get_Temperature(out var nTemp)) return -1000.0;

        return nTemp;
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
                    Log.Error("相机错误");
                    break;
                case Nncam.eEVENT.EVENT_DISCONNECTED:
                    _nncam?.Close();
                    _nncam = null;
                    Log.Error("相机断开");
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
        if (!_nncam.get_ExpoTime(out var expoTime)) return;
        if (!_nncam.get_Size(out var width, out var height)) return;
        var buffer = Marshal.AllocHGlobal(width * height * 6);
        if (!_nncam.PullImageV3(buffer, 0, 48, 0, out var info)) return;
        // buffer => mat
        var mat = new Mat(height, width, MatType.CV_16UC3, buffer);

        // 序列
        _seq++;
        try
        {
            Log.Information($"获取图片：Seq {_seq} Flag {_flag}");
            switch (_flag)
            {
                case "preview":
                {
                    // 关闭灯光
                    serialPort.WritePort("Com2", DefaultProtocol.CloseLight().ToBytes());
                    _photoList.Add(await SavePreviewAsync(mat, info, (int)expoTime));
                }
                    break;
                case "auto":
                {
                    /*
                     * 1. 保存白光图
                     * 2. 保存曝光图和合成图
                     */
                    switch (_seq)
                    {
                        // 暂存白光图
                        case 1:
                        {
                            // 关闭灯光
                            serialPort.WritePort("Com2", DefaultProtocol.CloseLight().ToBytes());
                            // 保存图片
                            _photoList.Add(await SaveAsync(mat, info, (int)expoTime));
                        }
                            break;
                        // 生成合成图
                        case 2:
                        {
                            // 保存图片
                            _photoList.Add(await SaveAsync(mat, info, (int)expoTime, true));
                            // 创建并保存合成图
                            var combine = await SaveCombineAsync();
                            if (combine != null) _photoList.Add(combine);
                        }
                            break;
                    }
                }
                    break;
                case "manual":
                {
                    /*
                     * 1. 保存白光图
                     * 2. 保存黑光图
                     * 其他. 多帧合成
                     */
                    switch (_seq)
                    {
                        case 1:
                            // 关闭灯光
                            serialPort.WritePort("Com2", DefaultProtocol.CloseLight().ToBytes());
                            // 保存图片
                            _photoList.Add(await SaveAsync(mat, info, (int)expoTime));
                            break;
                        case 2:
                            _mat = mat.Clone();
                            // 保存图片
                            _photoList.Add(await SaveAsync(mat, info, (int)expoTime, true));
                            break;
                        default:
                        {
                            var combine = new Mat(height, width, MatType.CV_16UC3, new Scalar(0));
                            try
                            {
                                if (_mat != null) Cv2.Add(mat, _mat, combine);
                                _mat?.Dispose();
                                _mat = combine.Clone();
                                // 保存图片
                                _photoList.Add(await SaveAsync(combine, info, (int)expoTime * (_seq - 1), true));
                            }
                            finally
                            {
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
                    var gray = new Mat();
                    try
                    {
                        // 保存图片
                        var filePath = Path.Combine(FileUtils.Collect, $"{_seq}_{expoTime}.png");
                        Cv2.CvtColor(mat, gray, ColorConversionCodes.BGR2GRAY);
                        gray.SaveImage(filePath);
                    }
                    finally
                    {
                        gray.Dispose();
                    }
                }
                    break;
                case "lost":
                    _mat = mat.Clone();
                    break;
                case "calibrate":
                {
                    var gray = new Mat();
                    try
                    {
                        var op = mat switch
                        {
                            { Width: 2992, Height: 3000 } => "3000",
                            { Width: 1488, Height: 1500 } => "1500",
                            { Width: 992, Height: 998 } => "1000",
                            _ => "3000"
                        };

                        var savePath = Path.Combine(FileUtils.Calibration, op);
                        if (!Directory.Exists(savePath)) Directory.CreateDirectory(savePath);
                        Cv2.CvtColor(mat, gray, ColorConversionCodes.BGR2GRAY);
                        Cv2.Normalize(gray, gray, 0, 65535.0, NormTypes.MinMax, MatType.CV_16UC1);
                        gray.SaveImage(Path.Combine(savePath, "before.png"));
                    }
                    finally
                    {
                        gray.Dispose();
                    }
                }
                    break;
            }
        }
        catch (Exception e)
        {
            Log.Error(e, "保存图片错误！");
        }
        finally
        {
            mat.Dispose();
            Marshal.FreeHGlobal(buffer);
        }
    }

    #endregion

    #region 保存图片

    private async Task<Photo> SaveAsync(
        Mat mat,
        Nncam.FrameInfoV3 info,
        int exposureTime,
        bool isDark = false
    )
    {
        var start = DateTime.Now;
        var original = new Mat();
        var thumb = new Mat();

        try
        {
            var prefix = isDark ? "D_" : "L_";
            var picName = $"{DateTime.Now:yyMMddHHmmssfff}";
            // 原始灰度图
            original = await CommonProcessAsync(mat);
            if (isDark)
            {
                // 保存原始图片
                var originalPath = Path.Combine(FileUtils.Original, $"O_{picName}.png");
                // 保存原始图片
                original.SaveImage(originalPath);
                await photo.AddReturnModel(new Photo
                {
                    AlbumId = _album?.Id ?? 0,
                    Name = $"O_{picName}",
                    Path = originalPath,
                    Width = (int)info.width,
                    Height = (int)info.height,
                    Type = -2,
                    ExposureTime = exposureTime,
                    Gain = info.expogain
                });
                // 黑白反色处理和LUT处理
                original = await DarkProcessAsync(original);
            }
            // 保存图片
            var picPath = Path.Combine(FileUtils.Exposure, $"{prefix}{picName}.png");
            // 保存灰度图处理后的文件
            original.SaveImage(picPath);

            // 转换成8位灰度图节省空间
            Cv2.ConvertScaleAbs(original, thumb, 255 / 65535.0);
            // 缩放
            Cv2.Resize(thumb, thumb, new Size(500, 500));
            // 缩略图路径
            var thumbPath = Path.Combine(FileUtils.Thumbnail, $"{picName}.jpg");
            // 保存缩略图文件
            thumb.SaveImage(thumbPath);

            return await photo.AddReturnModel(new Photo
            {
                AlbumId = _album?.Id ?? 0,
                Name = $"{prefix}{picName}",
                Path = picPath,
                Width = (int)info.width,
                Height = (int)info.height,
                Type = isDark ? 1 : 0,
                Thumbnail = thumbPath,
                ExposureTime = exposureTime,
                Gain = info.expogain
            });
        }
        catch (Exception e)
        {
            Log.Error(e, "保存图片错误！");
            throw;
        }
        finally
        {
            original.Dispose();
            thumb.Dispose();
            GC.Collect();
            var end = DateTime.Now;
            Log.Information($"保存图片耗时：{(end - start).TotalMilliseconds}ms");
        }
    }

    #endregion

    #region 保存合成图

    private async Task<Photo?> SaveCombineAsync()
    {
        var start = DateTime.Now;
        var mat1 = new Mat();
        var mat2 = new Mat();
        var multiply = new Mat();
        var thumb = new Mat();

        try
        {
            if (_photoList.Count != 2) return null;
            var light = _photoList.Find(p => p.Type == 0);
            var dark = _photoList.Find(p => p.Type == 1);
            if (light == null || dark == null) return null;

            mat1 = new Mat(light.Path, ImreadModes.AnyDepth);
            mat2 = new Mat(dark.Path, ImreadModes.AnyDepth);
            // 正片叠底将mat叠在-mat上面
            multiply = OpenCvUtils.Multiply(mat1, mat2);

            // 保存原图
            var picName = $"C_{DateTime.Now:yyMMddHHmmssfff}";
            // 保存图片
            var picPath = Path.Combine(FileUtils.Exposure, $"{picName}.png");
            multiply.SaveImage(picPath);
            // 保存缩略图
            //gray16 -> gray8
            Cv2.ConvertScaleAbs(multiply, thumb, 255 / 65535.0);
            Cv2.Resize(thumb, thumb, new Size(500, 500));
            var thumbPath = Path.Combine(FileUtils.Thumbnail, $"{picName}.jpg");
            thumb.SaveImage(thumbPath);

            return await photo.AddReturnModel(new Photo
            {
                AlbumId = _album?.Id ?? 0,
                Name = picName,
                Path = picPath,
                Width = dark.Width,
                Height = dark.Height,
                Type = 2,
                Thumbnail = thumbPath,
                ExposureTime = dark.ExposureTime,
                Gain = dark.Gain
            });
        }
        catch (Exception e)
        {
            Log.Error(e, "保存合成图错误！");
            throw;
        }
        finally
        {
            mat1.Dispose();
            mat2.Dispose();
            multiply.Dispose();
            thumb.Dispose();
            GC.Collect();
            var end = DateTime.Now;
            Log.Information($"保存合成图耗时：{(end - start).TotalMilliseconds}ms");
        }
    }

    #endregion

    #region 保存预览图

    private async Task<Photo> SavePreviewAsync(
        Mat mat,
        Nncam.FrameInfoV3 info,
        int exposureTime
    )
    {
        var start = DateTime.Now;
        var gray = new Mat();

        try
        {
            // 灰度图
            gray = await CommonProcessAsync(mat);
            // 保存路径
            var path = Path.Combine(FileUtils.Preview, $"{DateTime.Now:yyMMddHHmmssfff}.png");
            // 保存图片
            gray.SaveImage(path);
            // 保存缩略图
            return new Photo
            {
                Name = "Preview",
                Path = path,
                Width = (int)info.width,
                Height = (int)info.height,
                Type = -1,
                ExposureTime = exposureTime,
                Gain = info.expogain
            };
        }
        catch (Exception e)
        {
            Log.Error(e, "保存预览图错误！");
            throw;
        }
        finally
        {
            gray.Dispose();
            GC.Collect();
            var end = DateTime.Now;
            Log.Information($"保存预览图耗时：{(end - start).TotalMilliseconds}ms");
        }
    }

    #endregion

    #region 处理图片

    private async Task<Mat> CommonProcessAsync(Mat mat)
    {
        var cali = new Mat();
        var rotate = new Mat();
        var dst = new Mat();
        
        try
        {
            var op = mat switch
            {
                { Width: 2992, Height: 3000 } => "3000",
                { Width: 1488, Height: 1500 } => "1500",
                { Width: 992, Height: 998 } => "1000",
                _ => "3000"
            };
            var roi = await option.GetOptionValueAsync("Roi") ?? "0,1,0,1";
            var rot = await option.GetOptionValueAsync("Rotate") ?? "0";
            var matrix = await option.GetOptionValueAsync($"Matrix{op}") ?? "[]";
            var dist = await option.GetOptionValueAsync($"Dist{op}") ?? "[]";
            
            // 畸形校正
            cali = OpenCvUtils.Calibrate(mat, JsonConvert.DeserializeObject<double[,]>(matrix),
                JsonConvert.DeserializeObject<double[]>(dist));
            // 旋转
            rotate = OpenCvUtils.Rotate(cali, double.Parse(rot));
            // 截取
            dst = OpenCvUtils.CuteRoi(rotate, roi);
            // 灰度图
            var gray = new Mat();
            Cv2.CvtColor(dst, gray, ColorConversionCodes.BGR2GRAY);
            // 14 bit 转 16 bit
            gray.ConvertTo(gray, MatType.CV_16UC1, 4.0);

            return gray;
        }
        catch (Exception e)
        {
            cali.Dispose();
            rotate.Dispose();
            dst.Dispose();
            Log.Error(e, "处理图片错误！");
            return mat;
        }
    }

    private async Task<Mat> DarkProcessAsync(Mat mat)
    {
        try
        {
            // 目标灰度在直方图的占比
            var threshold = await option.GetOptionValueAsync("Threshold") ?? "0.0001";
            // 增强到目标灰度
            var enhanceThreshold = await option.GetOptionValueAsync("EnhanceThreshold") ?? "65535";
            // 计算直方图分布
            var hist = OpenCvUtils.Histogram(mat, double.Parse(threshold));
            // 计算比例
            var scale = double.Parse(enhanceThreshold) / hist;
            // 缩放
            mat.ConvertTo(mat, MatType.CV_16UC1, scale);
            // 反色
            Cv2.BitwiseNot(mat, mat);
            // 降噪
            var dstMat = new Mat();
            Cv2.MedianBlur(mat, dstMat, 5);
            // 返回
            return dstMat;
        }
        catch (Exception e)
        {
            Log.Error(e, "处理图片错误！");
            return mat;
        }
    }

    #endregion

    #region 计算曝光时间

    // snr 方式
    private async Task<long> ExpoWithSnr(double time, CancellationToken ctsToken)
    {
        if (_nncam == null) throw new Exception(localizer.GetString("Error0011").Value);
        // 设置曝光时间
        if (!_nncam.put_ExpoTime((uint)(time * 1000000))) throw new Exception(localizer.GetString("Error0009").Value);
        // 计算曝光时间
        if (!_nncam.Trigger(1)) throw new Exception(localizer.GetString("Error0010").Value);
        // 延时1300ms
        await Task.Delay((int)(time * 1000 + 1000), ctsToken);
        // 获取图片
        if (_mat == null) throw new Exception(localizer.GetString("Error0018").Value);
        // 计算信噪比
        var snr = OpenCvUtils.CalculateSnr(_mat, time);
        // 计算白色区域占比
        var percentage = OpenCvUtils.CalculatePercentage(_mat, 2550, 65535);
        // 清空_mat
        _mat?.Dispose();
        _mat = null;

        if (Math.Abs(time - 0.1) < 0.1)
            return percentage switch
            {
                <= 0.05 => snr switch
                {
                    <= -10 => GetScale(-50, -10, 1_000_000, 3_000_000, snr),
                    > -10 => await ExpoWithSnr(1, ctsToken),
                    _ => 1_500_000
                },
                <= 0.1 => GetScale(0.05, 0.1, 5_000_000, 10_000_000, percentage),
                <= 0.5 => GetScale(0.1, 0.5, 2_000_000, 5_000_000, percentage),
                _ => GetScale(0.5, 1, 100_000, 2_000_000, percentage)
            };


        if (Math.Abs(time - 1) < 0.1)
            return percentage switch
            {
                <= 0.05 => snr switch
                {
                    <= -15 => GetScale(-50, -15, 2_000_000, 3_000_000, snr),
                    <= 0 => GetScale(-15, 0, 3_000_000, 10_000_000, snr),
                    <= 1 => GetScale(0, 1, 10_000_000, 14_000_000, snr),
                    <= 1.5 => GetScale(1, 1.5, 14_000_000, 20_000_000, snr),
                    <= 2 => GetScale(1.5, 2, 20_000_000, 30_000_000, snr),
                    > 2 => await ExpoWithSnr(5, ctsToken),
                    _ => 2_000_000
                },
                <= 0.1 => GetScale(0.05, 0.1, 5_000_000, 10_000_000, percentage),
                <= 0.5 => GetScale(0.1, 0.5, 2_000_000, 5_000_000, percentage),
                _ => GetScale(0.5, 1, 100_000, 2_000_000, percentage)
            };

        if (Math.Abs(time - 5) < 0.1)
            return percentage switch
            {
                <= 0.05 => snr switch
                {
                    <= 2 => GetScale(0, 2, 5_000_000, 10_000_000, snr),
                    <= 5 => GetScale(2, 5, 10_000_000, 15_000_000, snr),
                    <= 6 => GetScale(5, 6, 15_000_000, 30_000_000, snr),
                    <= 7 => GetScale(6, 7, 30_000_000, 45_000_000, snr),
                    <= 7.5 => GetScale(7, 7.5, 45_000_000, 60_000_000, snr),
                    <= 8 => GetScale(7.5, 8, 60_000_000, 100_000_000, snr),
                    <= 20 => GetScale(8, 20, 100_000_000, 200_000_000, snr),
                    > 20 => 200_000_000,
                    _ => 15_000_000
                },
                <= 0.1 => GetScale(0.05, 0.1, 5_000_000, 10_000_000, percentage),
                <= 0.5 => GetScale(0.1, 0.5, 2_000_000, 5_000_000, percentage),
                _ => GetScale(0.5, 1, 30_000, 2_000_000, percentage)
            };

        return 0;
    }

    // 灰度方式
    private async Task<long> ExpoWithThreshold(CancellationToken ctsToken)
    {
        if (_nncam == null) throw new Exception(localizer.GetString("Error0011").Value);

        var start = DateTime.Now;
        var gray1S = new Mat();
        var gray2S = new Mat();

        try
        {
            var threshold = await option.GetOptionValueAsync("Threshold") ?? "0.0001";
            var targetThreshold = await option.GetOptionValueAsync("TargetThreshold") ?? "30000";

            // 拍摄1秒曝光
            _mat?.Dispose();
            _mat = null;
            if (!_nncam.put_ExpoTime(1_000_000)) throw new Exception(localizer.GetString("Error0009").Value);
            if (!_nncam.Trigger(1)) throw new Exception(localizer.GetString("Error0010").Value);
            await Task.Delay(1500, ctsToken);
            if (_mat == null) throw new Exception(localizer.GetString("Error0018").Value);
            Cv2.CvtColor(_mat, gray1S, ColorConversionCodes.BGR2GRAY);
            gray1S.ConvertTo(gray1S, MatType.CV_16UC1, 4.0);

            // 拍摄2秒曝光
            _mat?.Dispose();
            _mat = null;
            if (!_nncam.put_ExpoTime(2_000_000)) throw new Exception(localizer.GetString("Error0009").Value);
            if (!_nncam.Trigger(1)) throw new Exception(localizer.GetString("Error0010").Value);
            await Task.Delay(2500, ctsToken);
            if (_mat == null) throw new Exception(localizer.GetString("Error0018").Value);
            Cv2.CvtColor(_mat, gray2S, ColorConversionCodes.BGR2GRAY);
            gray2S.ConvertTo(gray2S, MatType.CV_16UC1, 4.0);

            var thr1 = OpenCvUtils.Histogram(gray1S, double.Parse(threshold));
            var thr2 = OpenCvUtils.Histogram(gray2S, double.Parse(threshold));

            if (thr1 == 0 || thr2 == 0)
            {
                return 300_000_000;
            }

            // x1 = 1, x2 = 2 => y1 = thr1, y2 = thr2
            // k = (y2 - y1) / (x2 - x1)
            // b = y1 - kx1
            // y = kx + b
            // x = (y - b) / k
            var k = thr2 - thr1;
            var b = thr1 - k;
            var target = (double.Parse(targetThreshold) - b) / k;

            var end = DateTime.Now;
            Log.Information($"计算曝光时间：target = {target} k = {k} b= {b} 耗时 = {(end - start).TotalMilliseconds}ms");

            return Math.Max(1000, Math.Min(300_000_000, (long)(target * 1_000_000)));
        }
        finally
        {
            gray1S.Dispose();
            gray2S.Dispose();
        }
    }

    #endregion

    #region 畸形校正

    public async Task Calibrate()
    {
        await InitAsync();
        if (_nncam == null)
            throw new Exception($"{localizer.GetString("Preview")}{localizer.GetString("Failure").Value}");
        // 设置flag
        _flag = "calibrate";
        // 目标张数
        _target = 3;
        // 序列
        _seq = 0;

        var ops = new[] { "3000", "1500", "1000" };

        try
        {
            Log.Information("开始校准");
            //打开灯光
            serialPort.WritePort("Com2", DefaultProtocol.OpenLight().ToBytes());
            // 延时100ms
            await Task.Delay(100);

            // 设置曝光时间
            var expoTime = uint.Parse(await option.GetOptionValueAsync("ExpoTime") ?? "30000");
            if (_nncam.put_ExpoTime(expoTime))
                Log.Information("设置曝光时间：" + expoTime);
            else
                throw new Exception(localizer.GetString("Error0009").Value);
            foreach (var op in ops)
            {
                var index = op switch
                {
                    "3000" => 0,
                    "1500" => 1,
                    "1000" => 2,
                    _ => 0
                };
                await SetPixel((uint)index);
                await Task.Delay(4000);
                // 触发拍摄
                if (_nncam.Trigger(1))
                    Log.Information("触发拍摄");
                else
                    throw new Exception(localizer.GetString("Error0010").Value);
                await Task.Delay(2000);
                Log.Information($"{op}校准拍摄完成！");
            }

            serialPort.WritePort("Com2", DefaultProtocol.CloseLight().ToBytes());
        }
        catch (Exception e)
        {
            serialPort.WritePort("Com2", DefaultProtocol.CloseLight().ToBytes());
            Log.Error(e, "校准失败！");
            throw;
        }


        foreach (var op in ops)
        {
            var images = Path.Combine(FileUtils.Calibration, op);
            if (!Directory.Exists(images) || Directory.GetFiles(images).Length <= 0) continue;
            var imagesList = Directory.GetFiles(images).ToList();
            OpenCvUtils.CalibrateCamera(imagesList, out var matrix, out var dist);
            if (matrix != null && dist != null)
            {
                Log.Information($"{op}畸形校正成功！");
                var strMatrix = JsonConvert.SerializeObject(matrix);
                var strDist = JsonConvert.SerializeObject(dist);
                await option.SetOptionValueAsync($"Matrix{op}", strMatrix);
                await option.SetOptionValueAsync($"Dist{op}", strDist);
                Log.Information($"Matrix{op}：" + strMatrix);
                Log.Information($"Dist{op}：" + strDist);
                var src = new Mat(imagesList[0], ImreadModes.Grayscale);
                var mask = new Mat();
                var newCameraMatrix =
                    Cv2.GetOptimalNewCameraMatrix(InputArray.Create(matrix), InputArray.Create(dist), src.Size(), 0,
                        src.Size(), out var roi);
                // cameraMatrix 数组转换成 Mat 类型
                Cv2.Undistort(src, mask, InputArray.Create(matrix), InputArray.Create(dist), newCameraMatrix);
                // 裁剪图片并返回原始尺寸
                var res = new Mat();
                Cv2.Resize(mask[roi], res, src.Size());
                res.SaveImage(Path.Combine(images, "after.png"));
            }
            else
            {
                Log.Error($"{op}畸形校正失败！");
            }
        }
    }

    #endregion

    #region 比例缩放

    private static long GetScale(double min, double max, double minTarget, double maxTarget, double value)
    {
        return (long)((maxTarget - minTarget) * (value - min) / (max - min) + minTarget);
    }

    #endregion

    #region 清除相册

    public async Task ClearAlbum()
    {
        if (_album != null)
        {
            await album.DeleteByIds([_album.Id]);
            _album = null;
        }
    }

    #endregion
}