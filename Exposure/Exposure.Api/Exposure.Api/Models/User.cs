using SqlSugar;

namespace Exposure.Api.Models;

[SugarTable("User")]
public class User
{
    [SugarColumn(IsPrimaryKey = true, IsIdentity = true)]
    public int Id { get; set; }
    public string Name { get; set; } = string.Empty;
    public string Sha { get; set; } = string.Empty;
    public int Role { get; set; }
    public bool Enabled { get; set; }
    public DateTime CreateTime { get; set; }
    public DateTime UpdateTime { get; set; }
    public DateTime LastLoginTime { get; set; }
}