using System.Diagnostics;
using System.Reflection;
using Exposure.Api.Contracts.Services;
using Exposure.Api.Models.Dto;
using Exposure.Protocal.Default;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Localization;
using OpenCvSharp;
using Serilog;

namespace Exposure.Api.Controllers;

[ApiController]
[Route("[controller]")]
public class MachineController(
    ISerialPortService serialPort,
    IStorageService storage,
    ICameraService camera,
    IOperLogService operLog,
    IErrorLogService errorLog,
    IAudioService audio,
    IUsbService usb,
    IStringLocalizer<SharedResources> localizer) : ControllerBase
{
    /**
     * led 0 全关， 1 绿色， 2 黄色， 3 黄慢闪， 4 黄快闪， 5 红色
     */
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
        if (!ports.Contains(dto.Port)) return Problem(localizer.GetString("PortNotExist").Value);
        // 将字符串hex转换为byte数组
        var hex = dto.Hex.Replace(" ", "");
        var bytes = new byte[hex.Length / 2];
        for (var i = 0; i < hex.Length; i += 2) bytes[i / 2] = Convert.ToByte(hex.Substring(i, 2), 16);
        serialPort.WritePort(dto.Port, bytes);
        Log.Information($"发送：{dto.Port} {BitConverter.ToString(bytes)}");
        return Ok();
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
        Log.Information($"发送：Com1 {BitConverter.ToString(protocol.ToBytes())}");
        return Ok();
    }

    #endregion

    #region 舱门

    [HttpGet]
    [Route("Hatch")]
    public async Task<ActionResult> Hatch([FromQuery] int code)
    {
        if (serialPort.GetFlag("led") <= 4)
        {
            serialPort.WritePort("Com1", DefaultProtocol.LedYellowFastFlash().ToBytes());
            serialPort.SetFlag("led", 4);
        }
        
        if (code == 1)
        {
            serialPort.SetFlag("hatch", 0);
            serialPort.WritePort("Com2", DefaultProtocol.OpenHatch().ToBytes());
            var num = 0;
            while (serialPort.GetFlag("hatch") == 0 && num < 50)
            {
                num++;
                await Task.Delay(100);
                if (num >= 40 && num % 2 == 0)
                {
                    serialPort.WritePort("Com2", DefaultProtocol.QueryOptocoupler().ToBytes());
                }
            }

            if (num >= 50)
                return Problem(localizer.GetString("OpenHatch").Value + localizer.GetString("Failure").Value);
            
            serialPort.SetFlag("hatch", 1);
            Log.Information(localizer.GetString("OpenHatch").Value);
        }
        else
        {
            serialPort.SetFlag("hatch", 1);
            serialPort.WritePort("Com2", DefaultProtocol.CloseHatch().ToBytes());
            var num = 0;
            while (serialPort.GetFlag("hatch") == 1 && num < 50)
            {
                num++;
                await Task.Delay(100);
            }

            if (num >= 50)
                return Problem(localizer.GetString("CloseHatch").Value + localizer.GetString("Failure").Value);
            serialPort.SetFlag("hatch", 0);
            Log.Information(localizer.GetString("CloseHatch").Value);
        }
        operLog.AddOperLog(localizer.GetString("OpenHatch").Value, code == 0 ? localizer.GetString("CloseHatch").Value : localizer.GetString("OpenHatch").Value);
        serialPort.WritePort("Com1", DefaultProtocol.LedAllClose().ToBytes());
        serialPort.SetFlag("led", 0);

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
        var ver1 = (Assembly.GetEntryAssembly() ?? throw new InvalidOperationException()).GetCustomAttribute<AssemblyInformationalVersionAttribute>()
            ?.InformationalVersion;
        var ver2 = serialPort.GetVer();
        
        var dict = new Dictionary<string, string?>
        {
            {"Ver1", ver1},
            {"Ver2", ver2}
        };
        
        return Ok(dict);
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

    #region 自检

    [HttpGet]
    [Route("SelfCheck")]
    public async Task<IActionResult> SelfCheck()
    {
        // 舱门自检
        try
        {
            serialPort.SetFlag("hatch", 1);
            serialPort.WritePort("Com2", DefaultProtocol.CloseHatch().ToBytes());
            var num = 0;
            while (serialPort.GetFlag("hatch") == 1 && num < 50)
            {
                num++;
                await Task.Delay(100);
                if (num >= 40 && num % 2 == 0)
                {
                    serialPort.WritePort("Com2", DefaultProtocol.QueryOptocoupler().ToBytes());
                }
            }

            if (num >= 50) throw new Exception(localizer.GetString("Error0012").Value);
        }
        catch (Exception e)
        {
            errorLog.AddErrorLog(e);
            Log.Error(e, localizer.GetString("Error0012").Value);
            serialPort.WritePort("Com1", DefaultProtocol.LedRed().ToBytes());
            serialPort.SetFlag("led", 5);
            audio.PlayWithSwitch("Error");
            return Problem(e.Message);
        }

        // 相机自检
        try
        {
            await camera.InitAsync();
        }
        catch (Exception e)
        {
            errorLog.AddErrorLog(e);
            Log.Error(e, localizer.GetString("Error0013").Value);
            serialPort.WritePort("Com1", DefaultProtocol.LedRed().ToBytes());
            serialPort.SetFlag("led", 5);
            audio.PlayWithSwitch("Error");
            return Problem(localizer.GetString("Error0012").Value + "-" + e.Message);
        }

        // 灯光自检
        try
        {
            await camera.PreviewAsync();
            await Task.Delay(1000);
            var res = await camera.GetCacheAsync();
            if (res.Count == 0) return Problem(localizer.GetString("Error0014").Value);
            var img = res[0];
            var mat = new Mat(img.Path);
            var gray = new Mat();
            var mask = new Mat();
            // 转换成灰度图
            Cv2.CvtColor(mat, gray, ColorConversionCodes.BGR2GRAY);
            var totalPixels = mat.Rows * mat.Cols;
            // 创建一个掩码，其中在指定范围内的像素为白色，其他像素为黑色
            Cv2.Threshold(gray, mask, 10, 255, ThresholdTypes.Binary);
            var aboveThresholdPixels = Cv2.CountNonZero(mask);
            var p = (double)aboveThresholdPixels / totalPixels;
            if (p < 0.1) throw new Exception(localizer.GetString("Error0014").Value);
        }
        catch (Exception e)
        {
            errorLog.AddErrorLog(e);
            Log.Error(e, localizer.GetString("Error0014").Value);
            serialPort.WritePort("Com1", DefaultProtocol.LedRed().ToBytes());
            serialPort.SetFlag("led", 5);
            audio.PlayWithSwitch("Error");
            return Problem(localizer.GetString("Error0014").Value + "-" + e.Message);
        }

        audio.PlayWithSwitch("Start");
        return Ok();
    }

    #endregion

    #region 更新

    [HttpGet]
    [Route("Update")]
    public IActionResult Update()
    {
        var usb1 = usb.GetDefaultUsbDrive();
        if (usb1 == null) throw new Exception(localizer.GetString("NoUsb").Value);
        var path = Path.Combine(usb1.Name, "Exposure");
        if (!Directory.Exists(path)) throw new Exception(localizer.GetString("NotFound"));
        var bat = Path.Combine(path, "Update.bat");
        if (!System.IO.File.Exists(bat)) throw new Exception(localizer.GetString("NotFound"));
        var batchProcess = new Process();
        batchProcess.StartInfo.FileName = bat;
        batchProcess.EnableRaisingEvents = true;
        batchProcess.Start();
        return Ok();
    }

    #endregion

    #region 关机

    [HttpGet]
    [Route("Shutdown")]
    public IActionResult Shutdown()
    {
        var process = new Process
        {
            StartInfo = new ProcessStartInfo
            {
                FileName = "shutdown",
                Arguments = "/s /t 0",
                UseShellExecute = false,
                CreateNoWindow = true
            }
        };
        process.Start();
        return Ok();
    }

    #endregion
    
}