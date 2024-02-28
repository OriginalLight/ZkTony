using Exposure.Api.Contracts.Services;
using Exposure.Api.Core.SerialPort.Default;
using Exposure.Api.Models.Dto;
using Microsoft.AspNetCore.Mvc;

namespace Exposure.Api.Controllers;

[ApiController]
[Route("[controller]")]
public class MetricController : ControllerBase
{
    private readonly ISerialPortService _serialPort;
    private readonly ICameraService _camera;
    private readonly IUsbService _usb;

    #region 构造函数

    /// <inheritdoc />
    public MetricController(ISerialPortService serialPort, IUsbService usb, ICameraService camera)
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
    public IActionResult Status()
    {
        var temperature = _camera.GetTemperature();
        var flag = _serialPort.GetFlag("led");
        var hatch = _serialPort.GetFlag("hatch");
        
        var dto = new StatusOutDto
        {
            Usb = _usb.IsUsbAttached(),
            Hatch = hatch == 1,
            Temperature = temperature
        };

        if (temperature > -18)
        {
            if (flag >= 3) return Ok(dto);
            _serialPort.WritePort("Com1", DefaultProtocol.LedYellow().ToBytes());
            _serialPort.SetFlag("led", 3);
        }
        else
        {
            if (flag >= 2) return Ok(dto);
            _serialPort.WritePort("Com1", DefaultProtocol.LedGreen().ToBytes());
            _serialPort.SetFlag("led", 2);
        }
        
        return Ok(dto);
    }

    #endregion
}