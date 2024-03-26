namespace Exposure.Api.Core.SerialPort.Default;

public class DefaultProtocol
{
    #region LED

     public static Protocol LedRed()
    {
        return new Protocol
        {
            Address = 0x01,
            Function = 0x10,
            Data = [0x00, 0x01]
        };
    }
    
    public static Protocol LedGreen()
    {
        return new Protocol
        {
            Address = 0x01,
            Function = 0x10,
            Data = [0x01, 0x01]
        };
    }
    
    public static Protocol LedBlue()
    {
        return new Protocol
        {
            Address = 0x01,
            Function = 0x10,
            Data = [0x02, 0x01]
        };
    }
    
    public static Protocol LedYellow()
    {
        return new Protocol
        {
            Address = 0x01,
            Function = 0x10,
            Data = [0x03, 0x01]
        };
    }
    
    public static Protocol LedPurple()
    {
        return new Protocol
        {
            Address = 0x01,
            Function = 0x10,
            Data = [0x04, 0x01]
        };
    }
    
    public static Protocol LedWhite()
    {
        return new Protocol
        {
            Address = 0x01,
            Function = 0x10,
            Data = [0x05, 0x01]
        };
    }
    
    // 快闪
    public static Protocol LedRedFastFlash()
    {
        return new Protocol
        {
            Address = 0x01,
            Function = 0x10,
            Data = [0x06, 0x01]
        };
    }
    
    public static Protocol LedGreenFastFlash()
    {
        return new Protocol
        {
            Address = 0x01,
            Function = 0x10,
            Data = [0x07, 0x01]
        };
    }
    
    public static Protocol LedBlueFastFlash()
    {
        return new Protocol
        {
            Address = 0x01,
            Function = 0x10,
            Data = [0x08, 0x01]
        };
    }
    
    public static Protocol LedYellowFastFlash()
    {
        return new Protocol
        {
            Address = 0x01,
            Function = 0x10,
            Data = [0x09, 0x01]
        };
    }
    
    public static Protocol LedPurpleFastFlash()
    {
        return new Protocol
        {
            Address = 0x01,
            Function = 0x10,
            Data = [0x0A, 0x01]
        };
    }
    
    public static Protocol LedWhiteFastFlash()
    {
        return new Protocol
        {
            Address = 0x01,
            Function = 0x10,
            Data = [0x0B, 0x01]
        };
    }
    
    // 慢闪
    public static Protocol LedRedSlowFlash()
    {
        return new Protocol
        {
            Address = 0x01,
            Function = 0x10,
            Data = [0x0C, 0x01]
        };
    }
    
    public static Protocol LedGreenSlowFlash()
    {
        return new Protocol
        {
            Address = 0x01,
            Function = 0x10,
            Data = [0x0D, 0x01]
        };
    }
    
    public static Protocol LedBlueSlowFlash()
    {
        return new Protocol
        {
            Address = 0x01,
            Function = 0x10,
            Data = [0x0E, 0x01]
        };
    }
    
    public static Protocol LedYellowSlowFlash()
    {
        return new Protocol
        {
            Address = 0x01,
            Function = 0x10,
            Data = [0x0F, 0x01]
        };
    }
    
    public static Protocol LedPurpleSlowFlash()
    {
        return new Protocol
        {
            Address = 0x01,
            Function = 0x10,
            Data = [0x10, 0x01]
        };
    }
    
    public static Protocol LedWhiteSlowFlash()
    {
        return new Protocol
        {
            Address = 0x01,
            Function = 0x10,
            Data = [0x11, 0x01]
        };
    }
    
    // 交替
    public static Protocol LedRedBlueFastAlternating()
    {
        return new Protocol
        {
            Address = 0x01,
            Function = 0x10,
            Data = [0x12, 0x01]
        };
    }
    
    public static Protocol LedRedBlueSlowAlternating()
    {
        return new Protocol
        {
            Address = 0x01,
            Function = 0x10,
            Data = [0x13, 0x01]
        };
    }
    
    public static Protocol LedRedGreenFastAlternating()
    {
        return new Protocol
        {
            Address = 0x01,
            Function = 0x10,
            Data = [0x14, 0x01]
        };
    }
    
    public static Protocol LedRedGreenSlowAlternating()
    {
        return new Protocol
        {
            Address = 0x01,
            Function = 0x10,
            Data = [0x15, 0x01]
        };
    }
    
    // 全关
    public static Protocol LedAllClose()
    {
        return new Protocol
        {
            Address = 0x01,
            Function = 0x10,
            Data = [0xFF, 0x00]
        };
    }

    #endregion

    #region 下位机

    public static Protocol OpenHatch()
    {
        return new Protocol
        {
            Address = 0x01,
            Function = 0x05,
            Data = [0x00, 0x01]
        };
    }
    
    public static Protocol CloseHatch()
    {
        return new Protocol
        {
            Address = 0x01,
            Function = 0x05,
            Data = [0x00, 0x00]
        };
    }
    
    public static Protocol OpenLight()
    {
        return new Protocol
        {
            Address = 0x01,
            Function = 0x05,
            Data = [0x01, 0x01]
        };
    }
    
    public static Protocol CloseLight()
    {
        return new Protocol
        {
            Address = 0x01,
            Function = 0x05,
            Data = [0x01, 0x00]
        };
    }
    
    public static Protocol OpenCameraPower()
    {
        return new Protocol
        {
            Address = 0x01,
            Function = 0x05,
            Data = [0x02, 0x01]
        };
    }
    
    public static Protocol CloseCameraPower()
    {
        return new Protocol
        {
            Address = 0x01,
            Function = 0x05,
            Data = [0x02, 0x00]
        };
    }
    
    public static Protocol OpenScreenPower()
    {
        return new Protocol
        {
            Address = 0x01,
            Function = 0x05,
            Data = [0x03, 0x01]
        };
    }
    
    public static Protocol CloseScreenPower()
    {
        return new Protocol
        {
            Address = 0x01,
            Function = 0x05,
            Data = [0x03, 0x00]
        };
    }
    
    //光耦
    public static Protocol QueryOptocoupler()
    {
        return new Protocol
        {
            Address = 0x01,
            Function = 0x04,
            Data = [0x00]
        };
    }
    
    public static Protocol HatchStep(int step)
    {
        // step 转换成4个字节小端序
        var data = BitConverter.GetBytes(step);
        if (BitConverter.IsLittleEndian == false)
            Array.Reverse(data);
        return new Protocol
        {
            Address = 0x01,
            Function = 0x07,
            Data = [0x01, data[0], data[1], data[2], data[3]]
        };
    }
    
    public static Protocol HatchOffset(int offset)
    {
        // offset 转换成4个字节小端序
        var data = BitConverter.GetBytes(offset);
        if (BitConverter.IsLittleEndian == false)
            Array.Reverse(data);
        return new Protocol
        {
            Address = 0x01,
            Function = 0x07,
            Data = [0x00, data[0], data[1], data[2], data[3]]
        };
    }

    #endregion
}