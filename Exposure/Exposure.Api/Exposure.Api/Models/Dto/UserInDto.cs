namespace Exposure.Api.Models.Dto;

public class UserInDto
{
    public long Id { get; set; }
    public string Name { get; set; }
    public string Password { get; set; }
    public int Role { get; set; }
    public bool Enabled { get; set; }
    public string Expire { get; set; }
}