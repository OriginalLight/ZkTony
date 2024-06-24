using Exposure.Api.Contracts.Services;
using Exposure.Api.Models.Dto;
using Exposure.Protocal.Default;
using Microsoft.AspNetCore.Mvc;
using Serilog;

namespace Exposure.Api.Controllers;

[ApiController]
[Route("[controller]")]
public class MetricController(
    ISerialPortService serialPort,
    IUsbService usb,
    ICameraService camera,
    IOptionService option)
    : ControllerBase
{
    #region 状态

    [HttpGet]
    public IActionResult Metric()
    {
        var temperature = camera.GetTemperature();
        var flag = serialPort.GetFlag("led");
        var hatch = serialPort.GetFlag("hatch");

        var dto = new MetricOutDto
        {
            Usb = usb.IsUsbAttached(),
            Hatch = hatch == 1,
            Temperature = temperature
        };

        var target = double.Parse(option.GetOptionValue("Temperature") ?? "-150");

        Log.Information("当前温度：" + temperature + "，目标温度：" + target + "，三色灯：" + flag + "，舱门：" + hatch);

        if (temperature > -50)
        {
            if (flag >= 2) return Ok(dto);
            serialPort.WritePort("Com1", DefaultProtocol.LedYellow().ToBytes());
            serialPort.SetFlag("led", 2);
        }
        else
        {
            if (flag >= 1) return Ok(dto);
            serialPort.WritePort("Com1", DefaultProtocol.LedGreen().ToBytes());
            serialPort.SetFlag("led", 1);
        }

        return Ok(dto);
    }

    #endregion
}