namespace Exposure.Api.Core.SerialPort;

public class Protocol
{
    private byte Head { get; set; } = 0xEE;
    public byte Address { get; set; } = 0x01;
    public byte Function { get; set; } = 0x01;
    private byte[] DataLength { get; set; } = Array.Empty<byte>();
    public byte[] Data { get; set; } = Array.Empty<byte>();
    private byte[] End { get; set; } = [0xFF, 0xFC];
    
    public byte[] ToBytes()
    {
        var arr = new List<byte> {Head, Address, Function};
        // 低位在前，高位在后
        DataLength = [(byte) (Data.Length & 0xFF), (byte) (Data.Length >> 8)];
        arr.AddRange(DataLength);
        arr.AddRange(Data);
        arr.AddRange(Crc.ModbusCrc16(arr));
        arr.AddRange(End);
        return arr.ToArray();
    }
}