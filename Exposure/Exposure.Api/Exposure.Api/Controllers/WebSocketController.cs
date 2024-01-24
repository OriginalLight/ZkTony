using System.Net.WebSockets;
using System.Text;
using Exposure.Api.Contracts.Services;
using Exposure.Api.Helpers;
using Exposure.Api.Models.Dto;
using Microsoft.AspNetCore.Mvc;
using Newtonsoft.Json;

namespace Exposure.Api.Controllers;

[ApiController]
[Route("[controller]")]
public class WebSocketController : ControllerBase
{
    private readonly ICameraService _camera;
    private readonly IErrorLogService _errorLog;
    private readonly IUsbService _usb;
    private CancellationTokenSource _cts;

    public WebSocketController(IUsbService usb, ICameraService camera, IErrorLogService errorLog)
    {
        _usb = usb;
        _camera = camera;
        _errorLog = errorLog;
    }

    /// <summary>
    ///     获取状态
    /// </summary>
    [HttpGet]
    public async Task WebSocket()
    {
        if (HttpContext.WebSockets.IsWebSocketRequest)
        {
            using var webSocket = await HttpContext.WebSockets.AcceptWebSocketAsync();
            await HandleWebSocket(webSocket);
        }
        else
        {
            HttpContext.Response.StatusCode = 400;
        }
    }

    /// <summary>
    ///     状态
    /// </summary>
    /// <param name="webSocket"></param>
    private async Task HandleWebSocket(WebSocket webSocket)
    {
        var buffer = new byte[1024 * 4];
        var result = await webSocket.ReceiveAsync(new ArraySegment<byte>(buffer), CancellationToken.None);
        // 定时发送消息
        while (!result.CloseStatus.HasValue)
        {
            // 接收消息并去除空格
            var message = Encoding.UTF8.GetString(buffer).Trim('\0');
            var dto = JsonConvert.DeserializeObject<WebSocketDto>(message);
            try
            {
                if (dto != null)
                    switch (dto.Code)
                    {
                        // 判断是否为查询所有包
                        case "status":
                        {
                            var dict = new Dictionary<string, object>
                            {
                                { "code", "status" },
                                {
                                    "data", new Dictionary<string, object>
                                    {
                                        { "usb", _usb.IsUsbAttached() },
                                        { "temperature", _camera.GetTemperature() },
                                        { "door", 0 }
                                    }
                                }
                            };
                            var clientMsg = Encoding.UTF8.GetBytes(JsonHelper.Serialize(dict));
                            await webSocket.SendAsync(new ArraySegment<byte>(clientMsg, 0, clientMsg.Length),
                                result.MessageType, result.EndOfMessage, CancellationToken.None);
                            break;
                        }
                        /*// 初始化
                        case "init":
                            await _camera.InitializeAsync(webSocket);
                            break;
                        // 像素
                        case "pixel":
                            if (dto.Data != null) await _camera.SetPixelAsync(webSocket, uint.Parse(dto.Data["index"].ToString() ?? "0"));
                            break;
                        // 预览
                        case "preview":
                            await _camera.PreviewAsync(webSocket);
                            break;
                        //自动拍照
                        case "auto":
                        {
                            _cts = new CancellationTokenSource();
                            _ = _camera.TakeAutoPhotoAsync(webSocket, _cts.Token);
                        }
                            break;
                        // 手动拍照
                        case "manual":
                        {
                            _cts = new CancellationTokenSource();
                            if (dto.Data != null)
                            {
                                var exposure = int.Parse(dto.Data["exposure"].ToString() ?? "0");
                                var frame = int.Parse(dto.Data["frame"].ToString() ?? "0");
                                _ = _camera.TakeManualPhotoAsync(webSocket, exposure, frame, _cts.Token);
                            }
                        }
                            break;
                        // 取消
                        case "cancel":
                        {
                            await _camera.CancelAsync(webSocket);
                            break;
                        }*/
                    }
            }
            catch (Exception e)
            {
                var dict = new Dictionary<string, object>
                {
                    { "code", "error" },
                    { "data", e.Message }
                };
                var clientMsg = Encoding.UTF8.GetBytes(JsonHelper.Serialize(dict));
                await webSocket.SendAsync(new ArraySegment<byte>(clientMsg, 0, clientMsg.Length),
                    result.MessageType, result.EndOfMessage, CancellationToken.None);
                _errorLog.AddErrorLog(e);
            }

            // 清空缓存
            buffer = new byte[1024 * 4];
            // 接收消息
            result = await webSocket.ReceiveAsync(new ArraySegment<byte>(buffer), CancellationToken.None);
        }

        await webSocket.CloseAsync(result.CloseStatus.Value, result.CloseStatusDescription, CancellationToken.None);
    }
}