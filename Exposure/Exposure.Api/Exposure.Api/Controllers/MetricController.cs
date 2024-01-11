using System.Net.WebSockets;
using System.Text;
using Exposure.Api.Contracts.Services;
using Microsoft.AspNetCore.Mvc;
using Newtonsoft.Json;

namespace Exposure.Api.Controllers;

[ApiController]
[Route("[controller]")]
public class MetricController : ControllerBase
{
    private readonly IUsbService _usb;
    private readonly ICameraService _camera;

    public MetricController(IUsbService usb, ICameraService camera)
    {
        _usb = usb;
        _camera = camera;
    }

    /// <summary>
    ///  获取状态
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
    ///   状态
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
            try
            {
                switch (message)
                {
                    // 判断是否为心跳包
                    case "ping":
                    {
                        var dict = new Dictionary<string, Object>
                        {
                            { "code", "ping" },
                            { "ping", "pong" }
                        };
                        var clientMsg = Encoding.UTF8.GetBytes(JsonConvert.SerializeObject(dict));
                        await webSocket.SendAsync(new ArraySegment<byte>(clientMsg, 0, clientMsg.Length),
                            result.MessageType, result.EndOfMessage, CancellationToken.None);
                        break;
                    }
                    // 判断是否为查询Usb包
                    case "usb":
                    {
                        var dict = new Dictionary<string, Object>
                        {
                            { "code", "usb" },
                            { "usb", _usb.GetUsbDrives().Length }
                        };
                        var clientMsg = Encoding.UTF8.GetBytes(JsonConvert.SerializeObject(dict));
                        await webSocket.SendAsync(new ArraySegment<byte>(clientMsg, 0, clientMsg.Length),
                            result.MessageType, result.EndOfMessage, CancellationToken.None);
                        break;
                    }
                    // 判断是否为查询温度包
                    case "temperature":
                    {
                        var dict = new Dictionary<string, Object>
                        {
                            { "code", "temperature" },
                            { "temperature", _camera.GetTemperature() }
                        };
                        var clientMsg = Encoding.UTF8.GetBytes(JsonConvert.SerializeObject(dict));
                        await webSocket.SendAsync(new ArraySegment<byte>(clientMsg, 0, clientMsg.Length),
                            result.MessageType, result.EndOfMessage, CancellationToken.None);
                        break;
                    }
                    // 判断是否为查询舱门包
                    case "door":
                    {
                        var dict = new Dictionary<string, Object>
                        {
                            { "code", "door" },
                            { "door", 0 }
                        };
                        var clientMsg = Encoding.UTF8.GetBytes(JsonConvert.SerializeObject(dict));
                        await webSocket.SendAsync(new ArraySegment<byte>(clientMsg, 0, clientMsg.Length),
                            result.MessageType, result.EndOfMessage, CancellationToken.None);
                        break;
                    }
                    // 判断是否为查询所有包
                    case "all":
                    {
                        var dict = new Dictionary<string, Object>
                        {
                            { "code", "all" },
                            { "usb", _usb.GetUsbDrives().Length },
                            { "temperature", _camera.GetTemperature() },
                            { "door", 0 }
                        };
                        var clientMsg = Encoding.UTF8.GetBytes(JsonConvert.SerializeObject(dict));
                        await webSocket.SendAsync(new ArraySegment<byte>(clientMsg, 0, clientMsg.Length),
                            result.MessageType, result.EndOfMessage, CancellationToken.None);
                        break;
                    }
                }
            }
            catch (Exception e)
            {
                await webSocket.SendAsync(
                    new ArraySegment<byte>(Encoding.UTF8.GetBytes(e.Message), 0,
                        Encoding.UTF8.GetBytes(e.Message).Length),
                    result.MessageType, result.EndOfMessage, CancellationToken.None);
            }

            // 接收消息
            result = await webSocket.ReceiveAsync(new ArraySegment<byte>(buffer), CancellationToken.None);
        }

        await webSocket.CloseAsync(result.CloseStatus.Value, result.CloseStatusDescription, CancellationToken.None);
    }
}