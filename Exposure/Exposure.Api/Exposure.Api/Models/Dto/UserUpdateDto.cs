namespace Exposure.Api.Models.Dto;

public class UserUpdateDto
{
    public long Id { get; set; }
    public string Name { get; set; } = string.Empty;

    public string OldPassword { get; set; } = string.Empty;
    public string NewPassword { get; set; } = string.Empty;
    public int Role { get; set; }
    public bool Enabled { get; set; }
}