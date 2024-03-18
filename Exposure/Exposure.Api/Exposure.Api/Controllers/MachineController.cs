using Exposure.Api.Contracts.Services;
using Exposure.Api.Core.SerialPort.Default;
using Exposure.Api.Models.Dto;
using Microsoft.AspNetCore.Mvc;

namespace Exposure.Api.Controllers;

[ApiController]
[Route("[controller]")]
public class MachineController(ILogger<MachineController> logger, ISerialPortService serialPort, IStorageService storage)
    : ControllerBase
{
    #region 串口状态

    [HttpGet]
    [Route("SerialPort")]
    public IActionResult GetPorts()
    {
        return Ok(serialPort.GetPorts());
    }

    #endregion

    #region 串口发送

    [HttpPost]
    [Route("SerialPort")]
    public IActionResult SerialPort([FromBody] SerialPortDto dto)
    {
        var ports = serialPort.GetPorts();
        if (!ports.Contains(dto.Port)) return Problem("串口未初始化，或者串口不存在。");
        // 将字符串hex转换为byte数组
        var hex = dto.Hex.Replace(" ", "");
        var bytes = new byte[hex.Length / 2];
        for (var i = 0; i < hex.Length; i += 2) bytes[i / 2] = Convert.ToByte(hex.Substring(i, 2), 16);
        serialPort.WritePort(dto.Port, bytes);
        logger.LogInformation($"发送串口数据：{dto.Port} {BitConverter.ToString(bytes)}");
        return Ok("OK");
    }

    #endregion

    #region 三色灯

    [HttpGet]
    [Route("Led")]
    public IActionResult Led([FromQuery] int code)
    {
        var protocol = code switch
        {
            0 => DefaultProtocol.LedRed(),
            1 => DefaultProtocol.LedGreen(),
            2 => DefaultProtocol.LedBlue(),
            3 => DefaultProtocol.LedYellow(),
            4 => DefaultProtocol.LedPurple(),
            5 => DefaultProtocol.LedWhite(),
            6 => DefaultProtocol.LedRedFastFlash(),
            7 => DefaultProtocol.LedGreenFastFlash(),
            8 => DefaultProtocol.LedBlueFastFlash(),
            9 => DefaultProtocol.LedYellowFastFlash(),
            10 => DefaultProtocol.LedPurpleFastFlash(),
            11 => DefaultProtocol.LedWhiteFastFlash(),
            12 => DefaultProtocol.LedRedSlowFlash(),
            13 => DefaultProtocol.LedGreenSlowFlash(),
            14 => DefaultProtocol.LedBlueSlowFlash(),
            15 => DefaultProtocol.LedYellowSlowFlash(),
            16 => DefaultProtocol.LedPurpleSlowFlash(),
            17 => DefaultProtocol.LedWhiteSlowFlash(),
            18 => DefaultProtocol.LedRedBlueFastAlternating(),
            19 => DefaultProtocol.LedRedBlueSlowAlternating(),
            20 => DefaultProtocol.LedRedGreenFastAlternating(),
            21 => DefaultProtocol.LedRedGreenSlowAlternating(),
            255 => DefaultProtocol.LedAllClose(),
            _ => DefaultProtocol.LedAllClose()
        };
        serialPort.WritePort("Com1", protocol.ToBytes());
        logger.LogInformation($"发送串口数据：Com1 {BitConverter.ToString(protocol.ToBytes())}");
        return Ok("OK");
    }

    #endregion

    #region 舱门

    [HttpGet]
    [Route("Hatch")]
    public async Task<ActionResult> Hatch([FromQuery] int code)
    {
        if (code == 1)
        {
            if (serialPort.GetFlag("led") < 2)
            {
                serialPort.WritePort("Com1", DefaultProtocol.LedYellowFastFlash().ToBytes());
                serialPort.SetFlag("led", 2);
            }

            serialPort.SetFlag("hatch", 0);
            serialPort.WritePort("Com2", DefaultProtocol.OpenHatch().ToBytes());
            var num = 0;
            while (serialPort.GetFlag("hatch") == 0 && num < 50)
            {
                num++;
                await Task.Delay(100);
            }

            if (num >= 50) return Problem("打开舱门超时");
            serialPort.WritePort("Com1", DefaultProtocol.LedAllClose().ToBytes());
            serialPort.SetFlag("led", 0);
            serialPort.SetFlag("hatch", 1);
            logger.LogInformation("打开舱门");
        }
        else
        {
            if (serialPort.GetFlag("led") < 2)
            {
                serialPort.WritePort("Com1", DefaultProtocol.LedYellowFastFlash().ToBytes());
                serialPort.SetFlag("led", 2);
            }

            serialPort.SetFlag("hatch", 1);
            serialPort.WritePort("Com2", DefaultProtocol.CloseHatch().ToBytes());
            var num = 0;
            while (serialPort.GetFlag("hatch") == 1 && num < 50)
            {
                num++;
                await Task.Delay(100);
            }

            if (num >= 50) return Problem("关闭舱门超时");
            serialPort.WritePort("Com1", DefaultProtocol.LedAllClose().ToBytes());
            serialPort.SetFlag("led", 0);
            serialPort.SetFlag("hatch", 0);
            logger.LogInformation("关闭舱门");
        }

        return Ok();
    }

    #endregion

    #region 灯光

    [HttpGet]
    [Route("Light")]
    public ActionResult Light([FromQuery] int code)
    {
        var protocol = code switch
        {
            0 => DefaultProtocol.CloseLight(),
            1 => DefaultProtocol.OpenLight(),
            _ => DefaultProtocol.CloseLight()
        };
        serialPort.WritePort("Com2", protocol.ToBytes());
        serialPort.SetFlag("light", code);
        return Ok();
    }

    #endregion

    #region 相机

    [HttpGet]
    [Route("Camera")]
    public ActionResult Camera([FromQuery] int code)
    {
        var protocol = code switch
        {
            0 => DefaultProtocol.CloseCameraPower(),
            1 => DefaultProtocol.OpenCameraPower(),
            _ => DefaultProtocol.CloseCameraPower()
        };
        serialPort.WritePort("Com2", protocol.ToBytes());
        serialPort.SetFlag("camera", code);
        return Ok();
    }

    #endregion

    #region 屏幕

    [HttpGet]
    [Route("Screen")]
    public ActionResult Screen([FromQuery] int code)
    {
        var protocol = code switch
        {
            0 => DefaultProtocol.CloseScreenPower(),
            1 => DefaultProtocol.OpenScreenPower(),
            _ => DefaultProtocol.CloseScreenPower()
        };
        serialPort.WritePort("Com2", protocol.ToBytes());
        serialPort.SetFlag("screen", code);
        return Ok();
    }

    #endregion
    
    #region 设备版本
    
    [HttpGet]
    [Route("Version")]
    public ActionResult Version()
    {
        return Ok(GetType().Assembly.GetName().Version?.ToString() ?? "None");
    }
    
    #endregion

    #region 存储

    [HttpGet]
    [Route("Storage")]
    public ActionResult Storage()
    {
        var st = storage.AvailableStorage();
        return Ok(st);
    }

    #endregion
}