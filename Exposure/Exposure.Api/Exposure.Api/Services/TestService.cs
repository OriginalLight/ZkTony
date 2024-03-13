using Exposure.Api.Contracts.Services;
using Exposure.Api.Core.SerialPort.Default;
using Exposure.Api.Models.Dto;

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
            _agingTask = Task.Run(AgingTask);
        }
        else
        {
            if (_agingTask is { IsCanceled: false }) _agingTask.Dispose();
            _agingTask?.Dispose();
            serialPort.WritePort("Com2", DefaultProtocol.CloseLight().ToBytes());
            serialPort.WritePort("Com2", DefaultProtocol.CloseLight().ToBytes());
            serialPort.WritePort("Com1", DefaultProtocol.LedAllClose().ToBytes());
        }
    }

    #endregion

    #region 老化测试后台任务

    private async void AgingTask()
    {
        var cHatch = 0;
        var cLight = 0;
        var cLed = 0;
        while (_agingDto.IsAnyTrue())
        {
            if (_agingDto.Hatch)
            {
                if (cHatch == 0)
                {
                    serialPort.WritePort("Com2", DefaultProtocol.OpenHatch().ToBytes());
                    cHatch = 1;
                }
                else
                {
                    serialPort.WritePort("Com2", DefaultProtocol.CloseHatch().ToBytes());
                    cHatch = 0;
                }
            }

            if (_agingDto.Light)
            {
                if (cLight == 0)
                {
                    serialPort.WritePort("Com2", DefaultProtocol.OpenLight().ToBytes());
                    cLight = 1;
                }
                else
                {
                    serialPort.WritePort("Com2", DefaultProtocol.CloseLight().ToBytes());
                    cLight = 0;
                }
            }

            if (_agingDto.Led)
                switch (cLed)
                {
                    case 0:
                        serialPort.WritePort("Com1", DefaultProtocol.LedYellowFastFlash().ToBytes());
                        cLed = 1;
                        break;
                    case 1:
                        serialPort.WritePort("Com1", DefaultProtocol.LedYellowSlowFlash().ToBytes());
                        cLed = 2;
                        break;
                    case 2:
                        serialPort.WritePort("Com1", DefaultProtocol.LedYellow().ToBytes());
                        cLed = 3;
                        break;
                    case 3:
                        serialPort.WritePort("Com1", DefaultProtocol.LedGreen().ToBytes());
                        cLed = 4;
                        break;
                    case 4:
                        serialPort.WritePort("Com1", DefaultProtocol.LedRed().ToBytes());
                        cLed = 0;
                        break;
                }

            if (_agingDto.Camera) cameraService.AgingTest();
            await Task.Delay(10000);
        }
    }

    #endregion
}