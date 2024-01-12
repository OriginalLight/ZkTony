using System.Drawing;
using System.Drawing.Imaging;
using System.Net.WebSockets;
using System.Text;
using Exposure.Api.Contracts.Services;
using Exposure.Api.Helpers;
using Exposure.Api.Models;
using OpenCvSharp;

namespace Exposure.Api.Services;

public class CameraService : ICameraService
{
    private readonly IPictureService _picture;
    private readonly IUserService _user;
    private Bitmap? _bitmap;
    private Nncam? _nncam;

    public CameraService(IPictureService picture, IUserService user)
    {
        _picture = picture;
        _user = user;
    }

    /// <summary>
    ///     初始化 0: 成功 1: 无设备 2: 打开失败
    /// </summary>
    /// <returns></returns>
    public async Task<bool> InitializeAsync(WebSocket webSocket)
    {
        if (_nncam != null)
            // 已经初始化
            return true;

        var arr = Nncam.EnumV2();

        if (arr.Length <= 0)
        {
            await SendAsync(webSocket, "init", "无设备");
            return false;
        }

        _nncam = Nncam.Open(arr[0].id);
        if (_nncam == null)
        {
            await SendAsync(webSocket, "init", "打开失败");
            return false;
        }

        if (!SetCallBack(webSocket))
        {
            await SendAsync(webSocket, "init", "设置回调失败");
            _nncam?.Close();
            _nncam = null;
            return false;
        }

        await SendAsync(webSocket, "init", "初始化成功");
        return true;
    }

    /// <summary>
    ///     拍照
    /// </summary>
    /// <param name="webSocket"></param>
    public async Task TakePhotoAsync(WebSocket webSocket)
    {
        if (!await InitializeAsync(webSocket)) return;

        if (_nncam != null && _nncam.Trigger(1))
            await SendAsync(webSocket, "photo", "拍照成功");
        else
            await SendAsync(webSocket, "photo", "拍照失败");
    }

    public async Task TakeMultiplePhotoAsync(WebSocket webSocket, uint size)
    {
        if (!await InitializeAsync(webSocket)) return;

        if (_nncam != null && _nncam.get_Size(out var width, out var height))
        {
            var bitmapList = new List<byte[]>();
            for (var i = 0; i < size; i++)
            {
                // 创建一个IntPtr list 
                bitmapList.Add(new byte[width * height * 3]);
                if (_nncam.TriggerSync(0, bitmapList[i], 24, 0, out var info))
                {
                    // opencv合并曝光时间
                    var matList = new List<Mat>();
                    bitmapList.ForEach(item => matList.Add(new Mat(height, width, MatType.CV_8UC3, item)));
                    var mat = new Mat(height, width, MatType.CV_8UC3, new Scalar(0));
                    // 新的mat是原来的matList的和
                    matList.ForEach(item => Cv2.Add(mat, item, mat));
                    // Mat转Bitmap
                    var bmp = new Bitmap(mat.Cols, mat.Rows, (int)mat.Step(), PixelFormat.Format24bppRgb, mat.Data);
                    // 保存图片
                    await SaveAsync(webSocket, bmp, info);
                }
                else
                {
                    await SendAsync(webSocket, "multiple", "拍照失败");
                    break;
                }
            }
        }
        else
        {
            await SendAsync(webSocket, "multiple", "拍照失败");
        }
    }

    /// <summary>
    ///     设置曝光时间
    /// </summary>
    /// <param name="webSocket"></param>
    /// <param name="time"></param>
    public async Task SetExposureAsync(WebSocket webSocket, uint time)
    {
        if (!await InitializeAsync(webSocket)) return;

        if (_nncam != null && _nncam.put_ExpoTime(time))
        {
            _nncam.get_ExpoTime(out var exposureTime);
            await SendAsync(webSocket, "exposure", $"设置成功：{exposureTime}");
        }
        else
        {
            await SendAsync(webSocket, "exposure", "设置失败");
        }
    }

    /// <summary>
    ///     设置增益
    /// </summary>
    /// <param name="webSocket"></param>
    /// <param name="index"></param>
    /// <returns></returns>
    /// <exception cref="NotImplementedException"></exception>
    public async Task SetPixelAsync(WebSocket webSocket, uint index)
    {
        if (!await InitializeAsync(webSocket)) return;

        _nncam?.Stop();
        _nncam?.put_eSize(index);
        if (!SetCallBack(webSocket))
        {
            await SendAsync(webSocket, "pixel", "设置回调失败");
            _nncam?.Close();
            _nncam = null;
        }

        if (_nncam != null && _nncam.get_Size(out var width, out var height))
            await SendAsync(webSocket, "pixel", "设置成功: width: " + width + ", height: " + height);
        else
            await SendAsync(webSocket, "pixel", "设置失败");
    }

    /// <summary>
    ///     设置白平衡
    /// </summary>
    /// <returns></returns>
    public double GetTemperature()
    {
        if (_nncam == null) return -100.0;

        short nTemp;
        _nncam.get_Temperature(out nTemp);
        return nTemp / 10.0;
    }

    /// <summary>
    ///     设置回调
    /// </summary>
    /// <param name="webSocket"></param>
    /// <returns></returns>
    /// <exception cref="ArgumentOutOfRangeException"></exception>
    private bool SetCallBack(WebSocket webSocket)
    {
        if (_nncam == null) return false;

        if (_nncam.get_Size(out var width, out var height))
        {
            _nncam.put_Option(Nncam.eOPTION.OPTION_TRIGGER, 1);
            _nncam.put_AutoExpoEnable(false);
            _bitmap = new Bitmap(width, height, PixelFormat.Format24bppRgb);
            if (_nncam.StartPullModeWithCallback(evt =>
                {
                    switch (evt)
                    {
                        case Nncam.eEVENT.EVENT_ERROR:
                            OnEventError(webSocket);
                            break;
                        case Nncam.eEVENT.EVENT_DISCONNECTED:
                            OnEventDisconnected(webSocket);
                            break;
                        case Nncam.eEVENT.EVENT_IMAGE:
                            OnEventImage(webSocket);
                            break;
                    }
                }))
                return true;
        }

        return false;
    }

    /// <summary>
    ///     设备错误
    /// </summary>
    /// <param name="webSocket"></param>
    private async void OnEventError(WebSocket webSocket)
    {
        _nncam?.Close();
        _nncam = null;
        await SendAsync(webSocket, "error", "设备错误");
    }

    /// <summary>
    ///     断开连接
    /// </summary>
    /// <param name="webSocket"></param>
    private async void OnEventDisconnected(WebSocket webSocket)
    {
        _nncam?.Close();
        _nncam = null;
        await SendAsync(webSocket, "disconnected", "设备断开");
    }

    /// <summary>
    ///     获取图片
    /// </summary>
    /// <param name="webSocket"></param>
    private async void OnEventImage(WebSocket webSocket)
    {
        if (_nncam == null) return;
        var info = new Nncam.FrameInfoV3();
        var bOk = false;
        var data = _bitmap?.LockBits(new Rectangle(0, 0, _bitmap.Width, _bitmap.Height), ImageLockMode.WriteOnly,
            _bitmap.PixelFormat);
        try
        {
            if (data != null) bOk = _nncam.PullImageV3(data.Scan0, 0, 24, data.Stride, out info);
        }
        finally
        {
            if (data != null) _bitmap?.UnlockBits(data);
        }

        if (!bOk)
        {
            await SendAsync(webSocket, "image", "获取图片失败");
            return;
        }

        try
        {
            await SaveAsync(webSocket, _bitmap, info);
        }
        catch (Exception e)
        {
            await SendAsync(webSocket, "image", $"获取图片失败: {e.Message}");
        }
    }

    /// <summary>
    ///     发送消息
    /// </summary>
    /// <param name="webSocket"></param>
    /// <param name="code"></param>
    /// <param name="data"></param>
    private async Task SendAsync(WebSocket webSocket, string code, object data)
    {
        var dict = new Dictionary<string, object>
        {
            { "code", code },
            { "data", data }
        };
        var clientMsg = Encoding.UTF8.GetBytes(JsonHelper.Serialize(dict));
        await webSocket.SendAsync(new ArraySegment<byte>(clientMsg, 0, clientMsg.Length),
            WebSocketMessageType.Text, true, CancellationToken.None);
    }

    /// <summary>
    ///     保存图片
    /// </summary>
    /// <param name="webSocket"></param>
    /// <param name="bitmap"></param>
    /// <param name="info"></param>
    private async Task SaveAsync(WebSocket webSocket, Bitmap? bitmap, Nncam.FrameInfoV3 info)
    {
        if (_nncam == null) return;
        var savePath = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.MyPictures), "Exposure");
        if (!Directory.Exists(savePath)) Directory.CreateDirectory(savePath);
        var date = DateTime.Now.ToString("yyyyMMddHHmmss");
        var fileName = $"{date}.tiff";
        var filePath = Path.Combine(savePath, fileName);
        bitmap?.Save(filePath, ImageFormat.Tiff);
        var logged = _user.GetLogged();

        _nncam.get_ExpoTime(out var exposureTime);

        var pic = new Picture
        {
            UserId = logged?.Id ?? 0,
            Name = fileName,
            Path = filePath,
            Width = int.Parse(info.width.ToString()),
            Height = int.Parse(info.height.ToString()),
            ExposureTime = int.Parse(exposureTime.ToString()),
            ExposureGain = int.Parse(info.expogain.ToString()),
            BlackLevel = int.Parse(info.blacklevel.ToString()),
            IsDelete = false,
            CreateTime = DateTime.Now,
            UpdateTime = DateTime.Now,
            DeleteTime = DateTime.Now
        };

        await _picture.Add(pic);

        await SendAsync(webSocket, "image", pic);
    }
}