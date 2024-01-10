namespace Exposure.Api.Models.Dto;

public class UserOutDto
{
    public long Id { get; set; }
    public string Name { get; set; }
    public int Role { get; set; }
    public bool Enabled { get; set; }
    public DateTime Expire { get; set; }
    public DateTime CreateTime { get; set; }
    public DateTime UpdateTime { get; set; }
    public DateTime LastLoginTime { get; set; }
}