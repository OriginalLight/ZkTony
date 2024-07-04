namespace Exposure.Api.Models.Dto;

public class UserAddDto
{
    public string Name { get; set; } = string.Empty;
    public string Password { get; set; } = string.Empty;
    public int Role { get; set; }
    public bool Enabled { get; set; }
}