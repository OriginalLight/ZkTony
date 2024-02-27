using Exposure.Api.Contracts.Services;
using Exposure.Api.Core.SerialPort.Default;
using Exposure.Api.Models.Dto;
using Microsoft.AspNetCore.Mvc;

namespace Exposure.Api.Controllers;

[ApiController]
[Route("[controller]")]
public class MachineController : ControllerBase
{
    private readonly ISerialPortService _serialPort;
    
    public MachineController(ISerialPortService serialPort)
    {
        _serialPort = serialPort;
    }
    
    #region 串口状态
    
    [HttpGet]
    [Route("SerialPort")]
    public IActionResult SerialPortStatus()
    {
        return Ok(_serialPort.GetPorts());
    }
    
    #endregion
    
    #region 串口发送
    
    [HttpPost]
    [Route("SerialPort")]
    public IActionResult SerialPort([FromBody] SerialPortDto dto)
    {
        try
        {
            var ports = _serialPort.GetPorts();
            if (!ports.Contains(dto.Port))
            {
                return Problem("串口未初始化，或者串口不存在。");
            }
            // 将字符串hex转换为byte数组
            var hex = dto.Hex.Replace(" ", "");
            var bytes = new byte[hex.Length / 2];
            for (var i = 0; i < hex.Length; i += 2)
            {
                bytes[i / 2] = Convert.ToByte(hex.Substring(i, 2), 16);
            }
            _serialPort.WritePort(dto.Port, bytes);
            return Ok("OK");
        }
        catch (Exception e)
        {
            return Problem(e.Message);
        }
    }

    #endregion

    #region 三色灯
    
    [HttpGet]
    [Route("Led")]
    public IActionResult Led([FromQuery] int code)
    {
        var p = code switch
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
        _serialPort.WritePort("Com1", p.ToBytes());
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
            _serialPort.WritePort("Com2", DefaultProtocol.OpenHatch().ToBytes());
            await Task.Delay(5000);
            
        }
        else
        {
            _serialPort.WritePort("Com2", DefaultProtocol.CloseHatch().ToBytes());
            await Task.Delay(5000);
        }
        return Ok();
    }

    #endregion

    #region 灯光

    [HttpGet]
    [Route("Light")]
    public ActionResult Light([FromQuery] int code)
    {
        if (code == 1)
        {
            _serialPort.WritePort("Com2", DefaultProtocol.OpenLight().ToBytes());
        }
        else
        {
            _serialPort.WritePort("Com2", DefaultProtocol.CloseLight().ToBytes());
        }
        return Ok();
    }

    #endregion

    #region 相机

    [HttpGet]
    [Route("Camera")]
    public ActionResult Camera([FromQuery] int code)
    {
        if (code == 1)
        {
            _serialPort.WritePort("Com2", DefaultProtocol.OpenCameraPower().ToBytes());
        }
        else
        {
            _serialPort.WritePort("Com2", DefaultProtocol.CloseCameraPower().ToBytes());
        }
        return Ok();
    }

    #endregion

    #region 屏幕

    [HttpGet]
    [Route("Screen")]
    public ActionResult Screen([FromQuery] int code)
    {
        if (code == 1)
        {
            _serialPort.WritePort("Com2", DefaultProtocol.OpenScreenPower().ToBytes());
        }
        else
        {
            _serialPort.WritePort("Com2", DefaultProtocol.CloseScreenPower().ToBytes());
        }
        return Ok();
    }

    #endregion
    
    
}