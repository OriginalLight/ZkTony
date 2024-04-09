using System.Diagnostics;
using Exposure.Api.Contracts.Services;
using Exposure.Api.Models.Dto;
using Exposure.Protocal.Default;
using Exposure.Utils;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Localization;
using OpenCvSharp;

namespace Exposure.Api.Controllers;

[ApiController]
[Route("[controller]")]
public class MachineController(
    ILogger<MachineController> logger,
    ISerialPortService serialPort,
    IStorageService storage,
    ICameraService camera,
    IErrorLogService errorLog,
    IAudioService audio,
    IUsbService usb,
    IStringLocalizer<SharedResources> localizer) : ControllerBase
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
        if (!ports.Contains(dto.Port)) return Problem(localizer.GetString("PortNotExist").Value);
        // 将字符串hex转换为byte数组
        var hex = dto.Hex.Replace(" ", "");
        var bytes = new byte[hex.Length / 2];
        for (var i = 0; i < hex.Length; i += 2) bytes[i / 2] = Convert.ToByte(hex.Substring(i, 2), 16);
        serialPort.WritePort(dto.Port, bytes);
        logger.LogInformation($"Tx：{dto.Port} {BitConverter.ToString(bytes)}");
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
        logger.LogInformation($"Tx：Com1 {BitConverter.ToString(protocol.ToBytes())}");
        return Ok();
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

            if (num >= 50)
                return Problem(localizer.GetString("OpenHatch").Value + localizer.GetString("Failure").Value);
            serialPort.WritePort("Com1", DefaultProtocol.LedAllClose().ToBytes());
            serialPort.SetFlag("led", 0);
            serialPort.SetFlag("hatch", 1);
            logger.LogInformation(localizer.GetString("OpenHatch").Value);
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

            if (num >= 50)
                return Problem(localizer.GetString("CloseHatch").Value + localizer.GetString("Failure").Value);
            serialPort.WritePort("Com1", DefaultProtocol.LedAllClose().ToBytes());
            serialPort.SetFlag("led", 0);
            serialPort.SetFlag("hatch", 0);
            logger.LogInformation(localizer.GetString("CloseHatch").Value);
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
        var version = GetType().Assembly.GetName().Version;
        // 只使用主版本号和次版本号和修订号
        var ver = version?.ToString().Split('.').Take(3).Aggregate((a, b) => a + "." + b);
        return Ok(ver ?? "1.0.0");
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
                if (num == 35)
                {
                    serialPort.WritePort("Com2", DefaultProtocol.QueryOptocoupler().ToBytes());
                }
                await Task.Delay(100);
            }

            if (num >= 50) throw new Exception(localizer.GetString("Error0012").Value);
        }
        catch (Exception e)
        {
            errorLog.AddErrorLog(e);
            logger.LogError(e, localizer.GetString("Error0012").Value);
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
            logger.LogError(e, localizer.GetString("Error0013").Value);
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
            try
            {
                // 转换成灰度图
                Cv2.CvtColor(mat, gray, ColorConversionCodes.BGR2GRAY);
                var totalPixels = mat.Rows * mat.Cols;
                // 创建一个掩码，其中在指定范围内的像素为白色，其他像素为黑色
                Cv2.Threshold(gray, mask, 10, 255, ThresholdTypes.Binary);
                var aboveThresholdPixels = Cv2.CountNonZero(mask);
                var p = (double)aboveThresholdPixels / totalPixels;
                if (p < 0.1) throw new Exception(localizer.GetString("Error0014").Value);
            }
            finally
            {
                mat.Dispose();
                gray.Dispose();
                mask.Dispose();
            }
        }
        catch (Exception e)
        {
            errorLog.AddErrorLog(e);
            logger.LogError(e, localizer.GetString("Error0014").Value);
            serialPort.WritePort("Com1", DefaultProtocol.LedRed().ToBytes());
            serialPort.SetFlag("led", 5);
            audio.PlayWithSwitch("Error");
            return Problem(localizer.GetString("Error0014").Value + "-" + e.Message);
        }

        audio.PlayWithSwitch("Start");
        return Ok();
    }

    #endregion
    
    [HttpGet]
    [Route("Update")]
    public IActionResult Update()
    {
        var usb1 = usb.GetDefaultUsbDrive();
        if (usb1 == null) throw new Exception(localizer.GetString("NoUsb").Value);
        var path = Path.Combine(usb1.Name, "Exposure");
        // 复制文件到Documents
        var documents = Environment.GetFolderPath(Environment.SpecialFolder.MyDocuments);
        var dir = new DirectoryInfo(path);
        if (dir.Exists)
        {
            // 整个文件夹复制，包括所有文件和子文件夹
            FileUtils.DirectoryCopy(path, Path.Combine(documents, "Exposure"), true);
        }
        else
        {
            throw new Exception(localizer.GetString("NotFound"));
        }
        var bat = Path.Combine(Path.Combine(documents, "Exposure"), "Update.bat");
        if (!System.IO.File.Exists(bat)) throw new Exception(localizer.GetString("NotFound"));
        var process = new Process
        {
            StartInfo = new ProcessStartInfo
            {
                FileName = bat,
                UseShellExecute = false,
                RedirectStandardOutput = true,
                CreateNoWindow = true
            }
        };
        process.Start();
        return Ok();
    }
}