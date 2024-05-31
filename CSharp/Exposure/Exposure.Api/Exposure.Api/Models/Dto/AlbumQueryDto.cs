namespace Exposure.Api.Models.Dto;

public class AlbumQueryDto : PageInDto
{
    public string? Name { get; set; }
    public DateTime? StartTime { get; set; }
    public DateTime? EndTime { get; set; }
}