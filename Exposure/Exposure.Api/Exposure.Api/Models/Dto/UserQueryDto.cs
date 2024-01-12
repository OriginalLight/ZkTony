namespace Exposure.Api.Models.Dto;

public class UserQueryDto : PageInDto
{
    public string? Name { get; set; }
    public int? Role { get; set; }
    public bool? Enabled { get; set; }
}