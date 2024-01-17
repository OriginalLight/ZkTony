namespace Exposure.Api.Models.Dto;

public class UserAddDto
{
    public string Name { get; set; }
    public string Password { get; set; }
    public int Role { get; set; }
    public bool Enabled { get; set; }
}