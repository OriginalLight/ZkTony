using SqlSugar;

namespace Exposure.Api.Models;

[SugarTable("ErrorLog")]
public class ErrorLog
{
    /// <summary>
    ///     主键
    /// </summary>
    [SugarColumn(IsPrimaryKey = true, IsIdentity = true)]
    public int Id { get; set; }

    /// <summary>
    ///     错误的详细信息
    /// </summary>
    public string Message { get; set; } = string.Empty;

    /// <summary>
    ///     错误源
    /// </summary>
    public string? Source { get; set; }

    /// <summary>
    ///     错误发生时的堆栈跟踪
    /// </summary>
    public string? StackTrace { get; set; }

    /// <summary>
    ///     错误类型
    /// </summary>
    public string Type { get; set; } = string.Empty;

    /// <summary>
    ///     错误发生的时间
    /// </summary>
    public DateTime Time { get; set; }
}