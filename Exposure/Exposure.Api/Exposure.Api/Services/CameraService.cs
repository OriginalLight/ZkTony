using System.Drawing;
using System.Drawing.Imaging;
using System.Runtime.InteropServices;
using Exposure.Api.Contracts.Services;
using Exposure.Api.Models;
using OpenCvSharp;
using OpenCvSharp.Extensions;

namespace Exposure.Api.Services;

public class CameraService : ICameraService
{
    private readonly IPictureService _picture;
    private readonly IUserService _user;
    private readonly List<Bitmap> _bitmapList = [];
    private string _flag = "auto";
    private int _target;
    private Nncam? _nncam;
    private readonly List<Picture> _pictureList = [];

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
        if (!(_nncam.put_Option(Nncam.eOPTION.OPTION_TRIGGER, 1) && _nncam.put_AutoExpoEnable(false)))
            throw new Exception("参数设置失败");

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
        //TODO 打开灯光
        if (!_nncam.get_Size(out var width, out var height)) throw new Exception("预览失败");
        var bytes = new byte[width * height * 3];
        if (!_nncam.put_ExpoTime(100000) || !_nncam.TriggerSync(0, bytes, 24, 0, out var info))
            throw new Exception("预览失败");
        var bitmap = new Bitmap(width, height, PixelFormat.Format24bppRgb);
        var data = bitmap.LockBits(new Rectangle(0, 0, bitmap.Width, bitmap.Height), ImageLockMode.WriteOnly,
            bitmap.PixelFormat);
        try
        {
            Marshal.Copy(bytes, 0, data.Scan0, bytes.Length);
        }
        finally
        {
            bitmap.UnlockBits(data);
        }


        var savePath = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.MyPictures), "Preview");
        if (!Directory.Exists(savePath)) Directory.CreateDirectory(savePath);
        var date = DateTime.Now.ToString("yyyyMMddHHmmss");
        var filePath = Path.Combine(savePath, $"{date}.png");
        bitmap.Save(filePath, ImageFormat.Png);

        _nncam.get_ExpoTime(out var exposureTime);

        return new Picture
        {
            UserId = 0,
            Name = "Preview",
            Path = filePath,
            Width = int.Parse(info.width.ToString()),
            Height = int.Parse(info.height.ToString()),
            Type = 0,
            ExposureTime = int.Parse(exposureTime.ToString()),
            ExposureGain = int.Parse(info.expogain.ToString()),
            BlackLevel = int.Parse(info.blacklevel.ToString()),
            IsDelete = false,
            CreateTime = DateTime.Now,
            UpdateTime = DateTime.Now,
            DeleteTime = DateTime.Now
        };

        //TODO 关闭灯光
    }

    /// <summary>
    ///     自动拍照
    /// </summary>
    /// <param name="ctsToken"></param>
    /// <returns></returns>
    /// <exception cref="NotImplementedException"></exception>
    public Task TakeAutoPhotoAsync(CancellationToken ctsToken)
    {
        throw new NotImplementedException();
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
        _bitmapList.Clear();
        _pictureList.Clear();
        _flag = "manual";
        _target = frame + 1;
        //TODO 打开灯光
        if (!(_nncam.put_ExpoTime(100000) && _nncam.Trigger(1))) throw new Exception("拍摄白光图失败");

        await Task.Delay(500, ctsToken);

        // 设置曝光时
        var avgExposureTime = exposure / frame;
        if (!(_nncam.put_ExpoTime(uint.Parse(avgExposureTime.ToString())) &&
              _nncam.Trigger(ushort.Parse(frame.ToString()))))
            throw new Exception("设置曝光时间失败");
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
            if (size == index)
            {
                return;
            }
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
        if (_nncam == null || !_nncam.get_Temperature(out var nTemp)) 
        {
            return -100.0;
        }

        return nTemp / 10.0;
    }

    /// <summary>
    ///     设置回调
    /// </summary>
    /// <returns></returns>
    private bool SetCallBack()
    {
        if (_nncam == null) return false;
        if (_nncam.StartPullModeWithCallback(evt =>
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
            }))
            return true;

        return false;
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
        Nncam.FrameInfoV3 info;
        var bitmap = new Bitmap(width, height, PixelFormat.Format24bppRgb);
        bool bOk;
        var data = bitmap.LockBits(new Rectangle(0, 0, bitmap.Width, bitmap.Height), ImageLockMode.WriteOnly,
            bitmap.PixelFormat);
        try
        {
            bOk = _nncam.PullImageV3(data.Scan0, 0, 24, data.Stride, out info);
        }
        finally
        {
            bitmap.UnlockBits(data);
        }

        if (!bOk) return;

        switch (_flag)
        {
            case "manual":
            {
                _bitmapList.Add(bitmap);
                // opencv合并曝光时间
                if (_bitmapList.Count == 1)
                {
                    // TODO 关闭灯光
                    _nncam.get_ExpoTime(out var time);
                    // 保存图片
                    _pictureList.Add(await SaveAsync(bitmap, info, int.Parse(time.ToString())));
                }
                else
                {
                    var matList = new List<Mat>();
                    // 不循环第一张图片
                    _bitmapList.Skip(1).ToList().ForEach(item => matList.Add(item.ToMat()));
                    var mat = new Mat(height, width, MatType.CV_8UC3, new Scalar(0));
                    // 新的mat是原来的matList的和
                    matList.ForEach(item => Cv2.Add(mat, item, mat));
                    // Mat转Bitmap
                    var bmp = new Bitmap(mat.Cols, mat.Rows, (int)mat.Step(), PixelFormat.Format24bppRgb, mat.Data);
                    // 保存图片
                    _nncam.get_ExpoTime(out var time);
                    _pictureList.Add(await SaveAsync(bmp, info,
                        int.Parse((time * (_bitmapList.Count - 1)).ToString()), 1));
                }

                break;
            }
        }
    }
    
    /// <summary>
    ///     保存图片
    /// </summary>
    /// <param name="bitmap"></param>
    /// <param name="info"></param>
    /// <param name="exposureTime"></param>
    /// <param name="type"></param>
    /// <param name="folder"></param>
    /// <returns></returns>
    private async Task<Picture> SaveAsync(Bitmap bitmap, Nncam.FrameInfoV3 info, int exposureTime, int type = 0,
        string folder = "Exposure")
    {
        var savePath = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.MyPictures), folder);
        if (!Directory.Exists(savePath)) Directory.CreateDirectory(savePath);
        var date = DateTime.Now.ToString("yyyyMMddHHmmss");
        var filePath = Path.Combine(savePath, $"{date}.png");
        bitmap.Save(filePath, ImageFormat.Png);

        var pic = await _picture.AddReturnModel(new Picture
        {
            UserId = _user.GetLogged()?.Id ?? 0,
            Name = date,
            Path = filePath,
            Width = int.Parse(info.width.ToString()),
            Height = int.Parse(info.height.ToString()),
            Type = type,
            ExposureTime = exposureTime,
            ExposureGain = int.Parse(info.expogain.ToString()),
            BlackLevel = int.Parse(info.blacklevel.ToString()),
            IsDelete = false,
            CreateTime = DateTime.Now,
            UpdateTime = DateTime.Now,
            DeleteTime = DateTime.Now
        });

        return pic;
    }
}