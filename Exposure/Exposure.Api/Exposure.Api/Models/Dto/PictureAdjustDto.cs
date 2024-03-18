namespace Exposure.Api.Models.Dto;

public class PictureAdjustDto
{
    public int Id { get; set; }
    public int Brightness { get; set; }
    public int Contrast { get; set; }
    
    public bool Invert { get; set; }
    
    public int Code { get; set; }
}