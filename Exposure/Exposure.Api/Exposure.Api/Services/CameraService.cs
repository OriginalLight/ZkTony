using System.Drawing;
using System.Drawing.Imaging;
using System.Net.WebSockets;
using System.Text;
using Exposure.Api.Contracts.Services;
using Exposure.Api.Models;
using Newtonsoft.Json;

namespace Exposure.Api.Services;

[System.Runtime.Versioning.SupportedOSPlatform("windows")]
public class CameraService : ICameraService
{
    private Nncam? _nncam;
    private Bitmap? _bitmap;
    private readonly IPictureService _picture;
    private readonly IUserService _user;
    
    public CameraService(IPictureService picture , IUserService user)
    {
        _picture = picture;
        _user = user;
    }

    /// <summary>
    ///   初始化 0: 成功 1: 无设备 2: 打开失败
    /// </summary>
    /// <returns></returns>
    public async Task InitializeAsync(WebSocket webSocket)
    {
        if (_nncam != null)
        {
            await SendAsync(webSocket, "init", "已初始化");
            return;
        }
        var arr = Nncam.EnumV2();

        if (arr.Length <= 0)
        {
            await SendAsync(webSocket, "init", "无设备");
            return;
        }
        _nncam = Nncam.Open(arr[0].id);
        if (_nncam == null)
        {
            await SendAsync(webSocket, "init", "打开失败");
            return;
        }
        int width, height;

        if (_nncam.get_Size(out width, out height))
        {
            _nncam.put_Option(Nncam.eOPTION.OPTION_TRIGGER, 1);
            _nncam.put_AutoExpoEnable(false);
            _bitmap = new Bitmap(width, height, PixelFormat.Format24bppRgb);
            if (!_nncam.StartPullModeWithCallback(evt =>
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
            {
                await SendAsync(webSocket, "init", "打开失败");
                _nncam?.Close();
                _nncam = null;
            }
        }
        
        await SendAsync(webSocket, "init", "初始化成功");
    }

    /// <summary>
    ///  拍照
    /// </summary>
    /// <param name="webSocket"></param>
    public async Task TakePhotoAsync(WebSocket webSocket)
    {
        if (_nncam == null)
        {
            await SendAsync(webSocket, "photo", "未初始化");
            return;
        }

        if (_nncam.Trigger(1))
        {
            await SendAsync(webSocket, "photo", "拍照成功");
        }
        else
        {
            await SendAsync(webSocket, "photo", "拍照失败");
        }
        
    }

    /// <summary>
    ///  设置曝光时间
    /// </summary>
    /// <param name="webSocket"></param>
    /// <param name="time"></param>
    public async Task SetExposureAsync(WebSocket webSocket, uint time)
    {
        if (_nncam == null)
        {
            await SendAsync(webSocket, "exposure", "未初始化");
            return;
        }

        if ( _nncam.put_ExpoTime(time))
        {
            uint newTime;
            _nncam.get_ExpoTime(out newTime);
            await SendAsync(webSocket, "exposure", $"设置成功：{newTime}");
        }
        else
        {
            await SendAsync(webSocket, "exposure", "设置失败");
        }
    }
    
    /// <summary>
    ///  设置增益
    /// </summary>
    /// <param name="webSocket"></param>
    /// <returns></returns>
    /// <exception cref="NotImplementedException"></exception>
    public Task SetPixelAsync(WebSocket webSocket)
    {
        throw new NotImplementedException();
    }

    /// <summary>
    ///  设置白平衡
    /// </summary>
    /// <returns></returns>
    public double GetTemperature()
    {
        if (_nncam == null)
        {
            return -100.0;
        }
        
        short nTemp;
        _nncam.get_Temperature(out nTemp);
        return nTemp / 10.0;
    }
 
    private async void OnEventError(WebSocket webSocket)
    {
        _nncam?.Close();
        _nncam = null;
        await SendAsync(webSocket, "error", "设备错误");
    }
    
    private async void OnEventDisconnected(WebSocket webSocket)
    {
        _nncam?.Close();
        _nncam = null;
        await SendAsync(webSocket, "disconnected", "设备断开");
    }
    
    private async void OnEventImage(WebSocket webSocket)
    {
        if (_nncam == null) return;
        var info = new Nncam.FrameInfoV3();
        var bOk = false;
        var data = _bitmap?.LockBits(new Rectangle(0, 0, _bitmap.Width, _bitmap.Height), ImageLockMode.WriteOnly, _bitmap.PixelFormat);
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
            var savePath = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.MyPictures), "Exposure");
            if (!Directory.Exists(savePath))
            {
                Directory.CreateDirectory(savePath);
            }
            var date = DateTime.Now.ToString("yyyyMMddHHmmss");
            var fileName = $"{date}.tiff";
            var filePath = Path.Combine(savePath, fileName);
            _bitmap?.Save(filePath, ImageFormat.Tiff);
            var logged = _user.GetLogged();

            await _picture.AddReturnIdentity(new Picture
            {
                UserId = logged?.Id ?? 0,
                Name = fileName,
                Path = filePath,
                Width = int.Parse(info.width.ToString()),
                Height = int.Parse(info.height.ToString()),
                ExposureTime = int.Parse(info.expotime.ToString()),
                ExposureGain = int.Parse(info.expogain.ToString()),
                BlackLevel = int.Parse(info.blacklevel.ToString()),
                IsDelete = false,
                CreateTime = DateTime.Now,
                UpdateTime = DateTime.Now,
                DeleteTime = DateTime.Now
            });
            
            await SendAsync(webSocket, "image", "获取图片成功");
        }
        catch (Exception e)
        {
            await SendAsync(webSocket, "image", $"获取图片失败: {e.Message}");
        }
    }

    private async Task SendAsync(WebSocket webSocket, string code, string message)
    {
        var dict = new Dictionary<string, Object>
        {
            { "code", code },
            { code, message }
        };
        var clientMsg = Encoding.UTF8.GetBytes(JsonConvert.SerializeObject(dict));
        await webSocket.SendAsync(new ArraySegment<byte>(clientMsg, 0, clientMsg.Length),
            WebSocketMessageType.Text, true, CancellationToken.None);
    }
}