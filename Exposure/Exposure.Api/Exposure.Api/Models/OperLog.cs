using SqlSugar;

namespace Exposure.Api.Models;

[SugarTable("OperLog")]
public class OperLog
{
    /// <summary>
    ///     主键
    /// </summary>
    [SugarColumn(IsPrimaryKey = true, IsIdentity = true)]
    public int Id { get; set; }

    /// <summary>
    ///     用户ID
    /// </summary>
    public int UserId { get; set; }

    /// <summary>
    ///     操作类型
    /// </summary>
    public string Type { get; set; } = string.Empty;

    /// <summary>
    ///     操作的详细描述
    /// </summary>
    public string Description { get; set; } = string.Empty;

    /// <summary>
    ///     操作发生的时间
    /// </summary>
    public DateTime Time { get; set; }
}