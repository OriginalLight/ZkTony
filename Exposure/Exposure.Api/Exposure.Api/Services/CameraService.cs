using System.Runtime.InteropServices;
using Exposure.Api.Contracts.Services;
using Exposure.Api.Models;
using OpenCvSharp;

namespace Exposure.Api.Services;

public class CameraService : ICameraService
{
    private readonly IPictureService _picture;
    private readonly List<Picture> _pictureList = [];
    private readonly IUserService _user;
    private string _flag = "auto";
    private Mat? _mat;
    private Nncam? _nncam;
    private int _target;
    private int _seq;

    public CameraService(IPictureService picture, IUserService user)
    {
        _picture = picture;
        _user = user;
    }

    /// <summary>
    ///     初始化 0: 成功 1: 无设备 2: 打开失败
    /// </summary>
    /// <returns></returns>
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

    /// <summary>
    ///     预览
    /// </summary>
    /// <returns></returns>
    public Picture PreviewAsync()
    {
        Initialize();
        if (_nncam == null) throw new Exception("预览失败");

        // 获取像素
        if (!_nncam.get_Size(out var width, out var height)) throw new Exception("获取像素失败");

        var buffer = Marshal.AllocHGlobal(width * height * 3);
        var mat = new Mat();
        try
        {
            //TODO 打开灯光
            // 设置曝光时间
            if (!_nncam.put_ExpoTime(100000)) throw new Exception("设置曝光时间失败");
            // 拍摄
            if (!_nncam.TriggerSync(0, buffer, 24, 0, out var info)) throw new Exception("预览失败");
            mat = new Mat(height, width, MatType.CV_8UC3, buffer);

            var savePath = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.MyPictures), "Preview");
            if (!Directory.Exists(savePath)) Directory.CreateDirectory(savePath);
            var date = DateTime.Now.ToString("yyyyMMddHHmmss");
            var filePath = Path.Combine(savePath, $"{date}.png");
            // 转换成灰度图
            Cv2.CvtColor(mat, mat, ColorConversionCodes.BGR2GRAY);
            mat.SaveImage(filePath);
            
            return new Picture
            {
                UserId = 0,
                Name = "Preview",
                Path = filePath,
                Width = (int) info.width,
                Height = (int) info.height,
                Type = 0,
                ExposureTime = 100000,
                ExposureGain = info.expogain,
                BlackLevel = info.blacklevel,
                IsDelete = false,
                CreateTime = DateTime.Now,
                UpdateTime = DateTime.Now,
                DeleteTime = DateTime.Now
            };
        }
        finally
        {
            //TODO 关闭灯光
            // 释放资源
            mat.Dispose();
            Marshal.FreeHGlobal(buffer);
        }
    }

    /// <summary>
    ///     自动拍照
    /// </summary>
    /// <param name="ctsToken"></param>
    /// <returns></returns>
    /// <exception cref="NotImplementedException"></exception>
    public async Task<int> TakeAutoPhotoAsync(CancellationToken ctsToken)
    {
        Initialize();
        if (_nncam == null) return 0;

        _mat = null;
        _pictureList.Clear();
        _target = 3;
        _seq = 0;
        _flag = "auto";
        var targetExpo = 1000000;

        // 获取像素
        if (!_nncam.get_Size(out var width, out var height)) throw new Exception("获取像素失败");

        // 计算曝光时间
        var buffer = Marshal.AllocHGlobal(width * height * 3);
        var mat = new Mat();
        try
        {
            // 设置曝光时间
            if (!_nncam.put_ExpoTime(1000000)) throw new Exception("设置采样曝光时间失败");
            // 计算曝光时间
            if (!_nncam.TriggerSync(0, buffer, 24, 0, out _)) throw new Exception("计算曝光时间失败");
            if (ctsToken.IsCancellationRequested) return 0;
            mat = new Mat(height, width, MatType.CV_8UC3, buffer);
            // 转换成灰度图
            Cv2.CvtColor(mat, mat, ColorConversionCodes.BGR2GRAY);
            var expo = CalculateExpo(mat, targetExpo, 0.5);
            if (expo > 1000000 * 60 * 30)
            {
                throw new Exception("曝光时间过长");
            }
            targetExpo = (int)expo;
        }
        finally
        {
            // 释放资源
            mat.Dispose();
            Marshal.FreeHGlobal(buffer);
        }

        // 拍摄白光图
        var buffer1 = Marshal.AllocHGlobal(width * height * 3);
        var mat1 = new Mat();
        try
        {
            // TODO 打开灯光
            // 设置白光曝光时间
            if (!_nncam.put_ExpoTime(100000)) throw new Exception("设置白光曝光时间失败");

            if (!_nncam.TriggerSync(0, buffer1, 24, 0, out var info)) throw new Exception("拍摄白光图失败");
            if (ctsToken.IsCancellationRequested) return 0;
            mat1 = new Mat(height, width, MatType.CV_8UC3, buffer1);
            _mat = mat1.Clone();
            _pictureList.Add(await SaveAsync(mat1, info, 100000));
        }
        finally
        {
            //TODO 关闭灯光
            // 释放资源
            mat1.Dispose();
        }

        // 设置曝光时
        if (!_nncam.put_ExpoTime((uint) targetExpo)) throw new Exception("设置曝光图曝光时间失败");

        if (!_nncam.Trigger(1)) throw new Exception("拍摄曝光图失败");

        return targetExpo;
    }

    /// <summary>
    ///     手动拍照
    /// </summary>
    /// <param name="exposure"></param>
    /// <param name="frame"></param>
    /// <param name="ctsToken"></param>
    public async Task TakeManualPhotoAsync(int exposure, int frame, CancellationToken ctsToken)
    {
        Initialize();
        if (_nncam == null) return;

        _mat = null;
        _pictureList.Clear();
        _target = frame + 1;
        _seq = 0;
        _flag = "manual";

        // 获取像素
        if (!_nncam.get_Size(out var width, out var height)) throw new Exception("获取像素失败");

        var buffer = Marshal.AllocHGlobal(width * height * 3);
        var mat = new Mat();
        try
        {
            //TODO 打开灯光
            // 设置白光曝光时间
            if (!_nncam.put_ExpoTime(100000)) throw new Exception("设置白光曝光时间失败");
            // 拍摄
            if (!_nncam.TriggerSync(0, buffer, 24, 0, out var info)) throw new Exception("拍摄白光图失败");
            mat = new Mat(height, width, MatType.CV_8UC3, buffer);
            if (ctsToken.IsCancellationRequested) return;
            _pictureList.Add(await SaveAsync(mat, info, 100000));
        }
        finally
        {
            //TODO 关闭灯光
            // 释放资源
            mat.Dispose();
            Marshal.FreeHGlobal(buffer);
        }

        // 设置曝光时
        if (!_nncam.put_ExpoTime((uint) (exposure / frame))) throw new Exception("设置多帧曝光时间失败");

        if (!_nncam.Trigger((ushort) frame)) throw new Exception("拍摄曝光图失败");
    }

    /// <summary>
    ///     取消拍照
    /// </summary>
    public void CancelTask()
    {
        Initialize();
        if (!(_nncam != null && _nncam.Trigger(0))) throw new Exception("取消失败");
    }

    /// <summary>
    ///     获取缓存
    /// </summary>
    /// <returns></returns>
    /// <exception cref="NotImplementedException"></exception>
    public async Task<List<Picture>> GetCacheAsync()
    {
        var count = 10;
        if (_pictureList.Count == _target) return _pictureList;
        while (count > 0 && _pictureList.Count != _target)
        {
            await Task.Delay(200);
            count--;
        }

        return _pictureList;
    }

    /// <summary>
    ///     设置增益
    /// </summary>
    /// <param name="index"></param>
    /// <returns></returns>
    /// <exception cref="NotImplementedException"></exception>
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
        if (!SetCallBack())
        {
            _nncam?.Close();
            _nncam = null;
            throw new Exception("设置回调失败");
        }
    }

    /// <summary>
    ///     设置白平衡
    /// </summary>
    /// <returns></returns>
    public double GetTemperature()
    {
        if (_nncam == null || !_nncam.get_Temperature(out var nTemp)) return -100.0;

        return nTemp / 10.0;
    }

    /// <summary>
    ///     设置回调
    /// </summary>
    /// <returns></returns>
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

    /// <summary>
    ///     设备错误
    /// </summary>
    private void OnEventError()
    {
        _nncam?.Close();
        _nncam = null;
    }

    /// <summary>
    ///     断开连接
    /// </summary>
    private void OnEventDisconnected()
    {
        _nncam?.Close();
        _nncam = null;
    }

    /// <summary>
    ///     获取图片
    /// </summary>
    private async void OnEventImage()
    {
        if (_nncam == null) return;
        if (!_nncam.get_Size(out var width, out var height)) return;
        var buffer = Marshal.AllocHGlobal(width * height * 3);
        if (!_nncam.PullImageV3(buffer, 0, 24, 0, out var info)) return;

        var mat = new Mat(height, width, MatType.CV_8UC3, buffer);
        _seq++;
        try
        {
            // 转换成灰度图
            _nncam.get_ExpoTime(out var expoTime);
            if (_flag == "manual")
            {
                if (_mat == null)
                {
                    _mat = mat.Clone();
                    _pictureList.Add(await SaveAsync(mat, info, (int) expoTime * _seq, 1));
                }
                else
                {
                    var mat1 = new Mat(height, width, MatType.CV_8UC3, new Scalar(0));
                    try
                    {
                        Cv2.Add(mat, _mat, mat1);
                        _mat = mat1.Clone();
                        _pictureList.Add(await SaveAsync(mat1, info, (int) expoTime * _seq, 1));
                    }
                    finally
                    {
                        // 释放资源
                        mat1.Dispose();
                    }
                }
            }
            else
            {
                _pictureList.Add(await SaveAsync(mat, info, (int) expoTime * _seq, 1));

                if (_mat == null) return;
                var mat1 = new Mat(height, width, MatType.CV_8UC3, new Scalar(0));
                try
                {
                    Cv2.Add(mat, _mat, mat1);
                    _pictureList.Add(await SaveAsync(mat1, info, (int) expoTime * _seq, 2, 1));
                }
                finally
                {
                    // 释放资源
                    mat1.Dispose();
                }
            }
        }
        finally
        {
            // 释放资源
            mat.Dispose();
            Marshal.FreeHGlobal(buffer);
        }
    }

    /// <summary>
    ///     保存图片
    /// </summary>
    /// <param name="mat"></param>
    /// <param name="info"></param>
    /// <param name="exposureTime"></param>
    /// <param name="type"></param>
    /// <param name="offset"></param>
    /// <returns></returns>
    private async Task<Picture> SaveAsync(Mat mat, Nncam.FrameInfoV3 info, int exposureTime, int type = 0,
        int offset = 0)
    {
        var tmp = mat.Clone();
        // 保存原图
        var myPictures = Environment.GetFolderPath(Environment.SpecialFolder.MyPictures);
        var date = DateTime.Now.AddSeconds(offset).ToString("yyyyMMddHHmmss");

        var savePath = Path.Combine(myPictures, "Exposure");
        if (!Directory.Exists(savePath)) Directory.CreateDirectory(savePath);
        var filePath = Path.Combine(savePath, $"{date}.png");
        Cv2.CvtColor(tmp, tmp, ColorConversionCodes.BGR2GRAY);
        tmp.SaveImage(filePath);

        // 保存缩略图
        var thumbnail = mat.Clone();
        Cv2.Resize(mat, thumbnail, new Size(500, 500));
        var thumbnailPath = Path.Combine(myPictures, "Thumbnail");
        if (!Directory.Exists(thumbnailPath)) Directory.CreateDirectory(thumbnailPath);
        var thumbnailFilePath = Path.Combine(thumbnailPath, $"{date}.jpg");
        Cv2.CvtColor(thumbnail, thumbnail, ColorConversionCodes.BGR2GRAY);
        thumbnail.SaveImage(thumbnailFilePath);
        tmp.Dispose();
        thumbnail.Dispose();

        var pic = await _picture.AddReturnModel(new Picture
        {
            UserId = _user.GetLogged()?.Id ?? 0,
            Name = date,
            Path = filePath,
            Width = (int) info.width,
            Height = (int) info.height,
            Type = type,
            Thumbnail = thumbnailFilePath,
            ExposureTime = exposureTime,
            ExposureGain = info.expogain,
            BlackLevel = info.blacklevel,
            IsDelete = false,
            CreateTime = DateTime.Now,
            UpdateTime = DateTime.Now,
            DeleteTime = DateTime.Now
        });

        return pic;
    }

    /// <summary>
    ///     计算信噪比
    /// </summary>
    /// <param name="mat"></param>
    /// <param name="expo"></param>
    /// <param name="snr"></param>
    /// <returns></returns>
    private double CalculateExpo(Mat mat,int expo, double snr)
    {
        // 计算信噪比
        Cv2.MeanStdDev(mat, out var mean, out var stddev);
        var snr1 = mean.Val0 / stddev.Val0;
        //计算曝光时间比例
        var ratio = Math.Pow(snr / snr1, 2);
        //计算目标曝光时间
        return expo / ratio;
    }
}