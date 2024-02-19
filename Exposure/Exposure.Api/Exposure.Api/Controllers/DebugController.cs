using Exposure.Api.Contracts.Services;
using Exposure.Api.Core.SerialPort.Default;
using Microsoft.AspNetCore.Mvc;

namespace Exposure.Api.Controllers;

[ApiController]
[Route("[controller]")]
public class DebugController : ControllerBase
{
    private readonly ISerialPortService _serialPort;
    private readonly ICameraService _camera;
    private readonly IUsbService _usb;

    #region 构造函数

    /// <inheritdoc />
    public DebugController(IUsbService usb, ICameraService camera, ISerialPortService serialPort)
    {
        _usb = usb;
        _camera = camera;
        _serialPort = serialPort;
    }

    #endregion

    #region 状态

    /// <summary>
    ///     状态
    /// </summary>
    /// <returns></returns>
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
}