namespace Exposure.Protocal;

public class Crc
{
    // modbus crc16
    public static byte[] ModbusCrc16(IEnumerable<byte> data)
    {
        ushort crc = 0xFFFF;
        foreach (var t in data)
        {
            crc ^= t;
            for (var j = 0; j < 8; j++)
            {
                if ((crc & 0x0001) != 0)
                {
                    crc >>= 1;
                    crc ^= 0xA001;
                }
                else
                {
                    crc >>= 1;
                }
            }
        }

        return [(byte) (crc & 0xFF), (byte) (crc >> 8)];
    }
}