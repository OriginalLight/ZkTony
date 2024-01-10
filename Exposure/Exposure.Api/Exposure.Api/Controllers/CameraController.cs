using System.Net.WebSockets;
using System.Text;
using Exposure.Api.Contracts.Services;
using Microsoft.AspNetCore.Mvc;
using Newtonsoft.Json;

namespace Exposure.Api.Controllers;

[ApiController]
[Route("[controller]")]
public class CameraController: ControllerBase
{
    private readonly ICameraService _camera;
    
    public CameraController(ICameraService camera)
    {
        _camera = camera;
    }

    [HttpGet]
    public async Task Camera()
    {
        if (HttpContext.WebSockets.IsWebSocketRequest)
        {
            using var webSocket = await HttpContext.WebSockets.AcceptWebSocketAsync();
            await Operation(webSocket);
        }
        else
        {
            HttpContext.Response.StatusCode = 400;
        }
    }

    private async Task Operation(WebSocket webSocket)
    {
        var buffer = new byte[1024 * 4];
        var result = await webSocket.ReceiveAsync(new ArraySegment<byte>(buffer), CancellationToken.None);
        // 定时发送消息
        while (!result.CloseStatus.HasValue)
        {
            // 接收消息并去除空格
            var message = Encoding.UTF8.GetString(buffer).Trim('\0');
            var code = message.Split(":");
            switch (code[0])
            {
                // 判断是否为心跳包
                case "ping":
                    await webSocket.SendAsync(new ArraySegment<byte>("pong"u8.ToArray()), result.MessageType, result.EndOfMessage, CancellationToken.None);
                    break;
                // 判断是否为查询Usb包
                case "init":
                    await _camera.InitializeAsync(webSocket);
                    break;
                case "photo":
                    await _camera.TakePhotoAsync(webSocket);
                    break;
                case "exposure":
                    await _camera.SetExposureAsync(webSocket, uint.Parse(code[1]));
                    break;
            }
            // 接收消息
            result = await webSocket.ReceiveAsync(new ArraySegment<byte>(buffer), CancellationToken.None);
        }
        
        await webSocket.CloseAsync(result.CloseStatus.Value, result.CloseStatusDescription, CancellationToken.None);
    }
}