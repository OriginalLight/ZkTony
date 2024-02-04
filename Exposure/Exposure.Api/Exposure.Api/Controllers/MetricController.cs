using Exposure.Api.Contracts.Services;
using Exposure.Api.Models.Dto;
using Microsoft.AspNetCore.Mvc;

namespace Exposure.Api.Controllers;

[ApiController]
[Route("[controller]")]
public class MetricController : ControllerBase
{
    private readonly ICameraService _camera;
    private readonly IUsbService _usb;

    #region 构造函数

    /// <inheritdoc />
    public MetricController(IUsbService usb, ICameraService camera)
    {
        _usb = usb;
        _camera = camera;
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
        return new JsonResult(new StatusOutDto
        {
            Usb = _usb.IsUsbAttached(),
            Door = false,
            Temperature = _camera.GetTemperature()
        });
    }

    #endregion
}