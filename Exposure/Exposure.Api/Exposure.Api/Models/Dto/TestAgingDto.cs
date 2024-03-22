namespace Exposure.Api.Models.Dto;

public class TestAgingDto
{
    public bool Hatch { get; set; }
    public bool Led { get; set; }
    public bool Light { get; set; }
    public bool Camera { get; set; }
    
    public int Cycle { get; set; }
    
    public int Interval { get; set; }
    
    public bool IsAnyTrue()
    {
        return Hatch || Led || Light || Camera;
    }
}