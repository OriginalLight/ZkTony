using Exposure.Api.Contracts.Services;
using Exposure.Api.Models;
using Exposure.Protocal.Default;
using Microsoft.AspNetCore.Mvc;

namespace Exposure.Api.Controllers;

[ApiController]
[Route("[controller]")]
public class OptionController(
    ILogger<OptionController> logger,
    IOptionService option,
    ISerialPortService serialPort,
    IAudioService audio) : ControllerBase
{
    #region 读取

    [HttpGet]
    public async Task<ActionResult> Get([FromQuery] string key)
    {
        // 读取
        var res = await option.GetOptionValueAsync(key) ?? "None";
        logger.LogInformation("Get Optiom：" + key + " = " + res);
        return Ok(res);
    }

    #endregion


    #region 设置

    [HttpPost]
    public async Task<ActionResult> Set([FromBody] Option dto)
    {
        // 设置
        var res = await option.SetOptionValueAsync(dto.Key, dto.Value);
        logger.LogInformation("Set Option：" + dto.Key + " = " + dto.Value);
        if (res) SetOptionValueHook(dto.Key, dto.Value);
        return Ok(res);
    }

    #endregion

    #region 设置key的时候触发的操作

    private void SetOptionValueHook(string key, string value)
    {
        switch (key)
        {
            case "HatchStep":
                serialPort.WritePort("Com2", DefaultProtocol.HatchStep(int.Parse(value)).ToBytes());
                break;
            case "HatchOffset":
                serialPort.WritePort("Com2", DefaultProtocol.HatchOffset(int.Parse(value)).ToBytes());
                break;
            case "Sound":
                switch (value)
                {
                    case "1":
                        audio.Play("Assets/Ringtones/Ringtone.wav");
                        break;
                    case "2":
                        audio.Play("Assets/Voices/Voice.wav");
                        break;
                }

                break;
        }
    }

    #endregion
}