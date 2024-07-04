using Exposure.Api.Contracts.Services;
using Exposure.Api.Models;
using Exposure.Protocal.Default;
using Microsoft.AspNetCore.Mvc;
using Serilog;

namespace Exposure.Api.Controllers;

[ApiController]
[Route("[controller]")]
public class OptionController(
    IOptionService option,
    ISerialPortService serialPort,
    IAudioService audio,
    ICameraService camera) : ControllerBase
{
    #region 读取

    [HttpGet]
    public async Task<ActionResult> Get([FromQuery] string key)
    {
        // 读取
        var res = await option.GetOptionValueAsync(key) ?? "None";
        Log.Information("获取参数：" + key + " = " + res);
        return Ok(res);
    }

    #endregion

    #region 读取所有

    [HttpGet("All")]
    public async Task<ActionResult> GetAll()
    {
        // 读取
        var res = await option.GetAllAsync();
        Log.Information("获取所有参数");
        return Ok(res);
    }

    #endregion


    #region 设置

    [HttpPost]
    public async Task<ActionResult> Set([FromBody] Option dto)
    {
        // 设置
        var res = await option.SetOptionValueAsync(dto.Key, dto.Value);
        if (res) SetOptionValueHook(dto.Key, dto.Value);
        Log.Information("设置参数：" + dto.Key + " = " + dto.Value);
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
            case "Temperature":
                if (camera.Camera != null)
                {
                    var temp = short.Parse(value);
                    camera.Camera.put_Temperature(temp);
                }

                break;
            case "Gain":
                if (camera.Camera != null)
                {
                    var gain = ushort.Parse(value);
                    camera.Camera.put_ExpoAGain(gain);
                }

                break;
        }
    }

    #endregion
}