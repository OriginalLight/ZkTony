using Exposure.Api.Contracts.Services;
using Exposure.Api.Models.Dto;
using Exposure.Protocal.Default;
using Exposure.Utils;

namespace Exposure.Api.Services;

public class TestService(ICameraService cameraService, ISerialPortService serialPort)
    : ITestService
{
    private TestAgingDto _agingDto = new();
    private Task? _agingTask;

    #region 老化测试

    public void AgingTest(TestAgingDto dto)
    {
        _agingDto = dto;
        if (dto.IsAnyTrue())
        {
            _agingTask ??= Task.Run(AgingTask);
        }
        else
        {
            _agingTask?.Dispose();
            _agingTask = null;
        }
    }

    #endregion

    #region 老化测试后台任务

    private async void AgingTask()
    {
        var cLight = 0;
        var cLed = 0;
        var cycle = 0;
        while (_agingDto.IsAnyTrue() && ++cycle <= _agingDto.Cycle)
        {
            await SaveAgingLogAsync($"---------------------------第{cycle}次老化测试开始");
            if (_agingDto.Hatch)
            {
                var hatch = serialPort.GetFlag("hatch");
                if (hatch == 0)
                {
                    await SaveAgingLogAsync("当前舱门状态: 关闭, 下发打开舱门指令");
                    serialPort.WritePort("Com2", DefaultProtocol.OpenHatch().ToBytes());
                }
                else
                {
                    await SaveAgingLogAsync("当前舱门状态: 打开, 下发关闭舱门指令");
                    serialPort.WritePort("Com2", DefaultProtocol.CloseHatch().ToBytes());
                }
            }

            if (_agingDto.Light)
            {
                if (cLight == 0)
                {
                    await SaveAgingLogAsync("当前灯状态: 关闭, 下发打开灯指令");
                    serialPort.WritePort("Com2", DefaultProtocol.OpenLight().ToBytes());
                    cLight = 1;
                }
                else
                {
                    await SaveAgingLogAsync("当前灯状态: 打开, 下发关闭灯指令");
                    serialPort.WritePort("Com2", DefaultProtocol.CloseLight().ToBytes());
                    cLight = 0;
                }
            }

            if (_agingDto.Led)
                switch (cLed)
                {
                    case 0:
                        await SaveAgingLogAsync("当前LED状态: 红灯, 下发黄灯快闪指令");
                        serialPort.WritePort("Com1", DefaultProtocol.LedYellowFastFlash().ToBytes());
                        cLed = 1;
                        break;
                    case 1:
                        await SaveAgingLogAsync("当前LED状态: 黄灯快闪, 下发黄灯慢闪指令");
                        serialPort.WritePort("Com1", DefaultProtocol.LedYellowSlowFlash().ToBytes());
                        cLed = 2;
                        break;
                    case 2:
                        await SaveAgingLogAsync("当前LED状态: 黄灯慢闪, 下发黄灯常亮指令");
                        serialPort.WritePort("Com1", DefaultProtocol.LedYellow().ToBytes());
                        cLed = 3;
                        break;
                    case 3:
                        await SaveAgingLogAsync("当前LED状态: 黄灯常亮, 下发绿灯常亮指令");
                        serialPort.WritePort("Com1", DefaultProtocol.LedGreen().ToBytes());
                        cLed = 4;
                        break;
                    case 4:
                        await SaveAgingLogAsync("当前LED状态: 绿灯常亮, 下发红灯常亮指令");
                        serialPort.WritePort("Com1", DefaultProtocol.LedRed().ToBytes());
                        cLed = 0;
                        break;
                }

            if (_agingDto.Camera)
                try
                {
                    await SaveAgingLogAsync("开始摄像头老化测试");
                    var temp = cameraService.GetTemperature();
                    await SaveAgingLogAsync($"当前摄像头温度: {temp}");
                    await cameraService.AgingTest();
                }
                catch (Exception e)
                {
                    await SaveAgingLogAsync("摄像头老化测试异常: " + e.Message);
                }

            await SaveAgingLogAsync($"等待{_agingDto.Interval}秒后进行下一次老化测试");
            await Task.Delay(_agingDto.Interval * 1000);
            await SaveAgingLogAsync($"---------------------------第{cycle}次老化测试结束");
        }

        _agingTask = null;
    }

    #endregion


    #region 存储老化测试日志

    private async Task SaveAgingLogAsync(string msg)
    {
        // 存储老化测试日志
        var date = DateTime.Now.ToString("yyyyMMdd HH:mm:ss");
        var file = FileUtils.GetFileName(Environment.GetFolderPath(Environment.SpecialFolder.Desktop), "AgingLog.txt");
        if (File.Exists(file))
        {
            // 文件存在
            await using var sw = File.AppendText(file);
            await sw.WriteLineAsync(date + " " + msg);
        }
        else
        {
            // 文件不存在
            await using var sw = File.CreateText(file);
            await sw.WriteLineAsync(date + " " + msg);
        }
    }

    #endregion
}