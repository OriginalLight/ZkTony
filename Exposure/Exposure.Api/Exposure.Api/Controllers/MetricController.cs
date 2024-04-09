using Exposure.Api.Contracts.Services;
using Exposure.Api.Models.Dto;
using Exposure.Protocal.Default;
using Microsoft.AspNetCore.Mvc;

namespace Exposure.Api.Controllers;

[ApiController]
[Route("[controller]")]
public class MetricController(ISerialPortService serialPort, IUsbService usb, ICameraService camera)
    : ControllerBase
{
    #region 状态

    [HttpGet]
    public IActionResult Status()
    {
        var temperature = camera.GetTemperature();
        var flag = serialPort.GetFlag("led");
        var hatch = serialPort.GetFlag("hatch");

        var dto = new StatusOutDto
        {
            Usb = usb.IsUsbAttached(),
            Hatch = hatch == 1,
            Temperature = temperature
        };

        if (temperature > -13)
        {
            if (flag >= 3) return Ok(dto);
            serialPort.WritePort("Com1", DefaultProtocol.LedYellow().ToBytes());
            serialPort.SetFlag("led", 3);
        }
        else
        {
            if (flag >= 2) return Ok(dto);
            serialPort.WritePort("Com1", DefaultProtocol.LedGreen().ToBytes());
            serialPort.SetFlag("led", 2);
        }

        return Ok(dto);
    }

    #endregion
}