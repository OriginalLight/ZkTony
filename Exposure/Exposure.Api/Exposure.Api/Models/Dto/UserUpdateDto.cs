namespace Exposure.Api.Models.Dto;

public class UserUpdateDto
{
    public long Id { get; set; }
    public string Name { get; set; }

    public string OldPassword { get; set; }
    public string NewPassword { get; set; }
    public int Role { get; set; }
    public bool Enabled { get; set; }
}