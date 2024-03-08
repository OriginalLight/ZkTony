using Exposure.Api.Contracts.Services;
using Exposure.Api.Core.SerialPort.Default;
using Exposure.Api.Models.Dto;

namespace Exposure.Api.Services;

public class TestService : ITestService
{
    private readonly ICameraService _cameraService;
    private readonly ISerialPortService _serialPort;
    private TestAgingDto _agingDto = new();
    private Task? _agingTask;

    #region 构造函数

    public TestService(ICameraService cameraService, ISerialPortService serialPort)
    {
        _cameraService = cameraService;
        _serialPort = serialPort;
    }

    #endregion

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
            _serialPort.WritePort("Com2", DefaultProtocol.CloseLight().ToBytes());
            _serialPort.WritePort("Com2", DefaultProtocol.CloseLight().ToBytes());
            _serialPort.WritePort("Com1", DefaultProtocol.LedAllClose().ToBytes());
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
                    _serialPort.WritePort("Com2", DefaultProtocol.OpenHatch().ToBytes());
                    cHatch = 1;
                }
                else
                {
                    _serialPort.WritePort("Com2", DefaultProtocol.CloseHatch().ToBytes());
                    cHatch = 0;
                }
            }

            if (_agingDto.Light)
            {
                if (cLight == 0)
                {
                    _serialPort.WritePort("Com2", DefaultProtocol.OpenLight().ToBytes());
                    cLight = 1;
                }
                else
                {
                    _serialPort.WritePort("Com2", DefaultProtocol.CloseLight().ToBytes());
                    cLight = 0;
                }
            }

            if (_agingDto.Led)
                switch (cLed)
                {
                    case 0:
                        _serialPort.WritePort("Com1", DefaultProtocol.LedYellowFastFlash().ToBytes());
                        cLed = 1;
                        break;
                    case 1:
                        _serialPort.WritePort("Com1", DefaultProtocol.LedYellowSlowFlash().ToBytes());
                        cLed = 2;
                        break;
                    case 2:
                        _serialPort.WritePort("Com1", DefaultProtocol.LedYellow().ToBytes());
                        cLed = 3;
                        break;
                    case 3:
                        _serialPort.WritePort("Com1", DefaultProtocol.LedGreen().ToBytes());
                        cLed = 4;
                        break;
                    case 4:
                        _serialPort.WritePort("Com1", DefaultProtocol.LedRed().ToBytes());
                        cLed = 0;
                        break;
                }

            if (_agingDto.Camera) _cameraService.AgingTest();
            await Task.Delay(10000);
        }
    }

    #endregion
}