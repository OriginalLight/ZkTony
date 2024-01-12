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
    public async Task Metric()
    {
        if (HttpContext.WebSockets.IsWebSocketRequest)
        {
            using var webSocket = await HttpContext.WebSockets.AcceptWebSocketAsync();
            await Status(webSocket);
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
    private async Task Status(WebSocket webSocket)
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
                        // 判断是否为心跳包
                        case "ping":
                        {
                            var dict = new Dictionary<string, object>
                            {
                                { "code", "ping" },
                                { "data", "pong" }
                            };
                            var clientMsg = Encoding.UTF8.GetBytes(JsonHelper.Serialize(dict));
                            await webSocket.SendAsync(new ArraySegment<byte>(clientMsg, 0, clientMsg.Length),
                                result.MessageType, result.EndOfMessage, CancellationToken.None);
                            break;
                        }
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
                        // 初始化
                        case "init":
                            await _camera.InitializeAsync(webSocket);
                            break;
                        // 拍照
                        case "photo":
                            await _camera.TakePhotoAsync(webSocket);
                            break;
                        // 曝光时间
                        case "exposure":
                            if (dto.Data != null) await _camera.SetExposureAsync(webSocket, uint.Parse(dto.Data));
                            break;
                        // 像素
                        case "pixel":
                            if (dto.Data != null) await _camera.SetPixelAsync(webSocket, uint.Parse(dto.Data));
                            break;
                        // 多帧拍照
                        case "multiple":
                            if (dto.Data != null) await _camera.TakeMultiplePhotoAsync(webSocket, uint.Parse(dto.Data));
                            break;
                    }
            }
            catch (Exception e)
            {
                await webSocket.SendAsync(
                    new ArraySegment<byte>(Encoding.UTF8.GetBytes(e.Message), 0,
                        Encoding.UTF8.GetBytes(e.Message).Length),
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