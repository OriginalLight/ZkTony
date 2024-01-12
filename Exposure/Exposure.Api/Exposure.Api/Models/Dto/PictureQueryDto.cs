namespace Exposure.Api.Models.Dto;

public class PictureQueryDto : PageInDto
{
    public bool IsDeleted { get; set; }
    public string? Name { get; set; }
    public DateTime? StartTime { get; set; }
    public DateTime? EndTime { get; set; }
}