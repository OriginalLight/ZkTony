using SqlSugar;

namespace Exposure.Api.Models;

[SugarTable("Album")]
public class Album
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
    ///     图片名称
    /// </summary>
    public string Name { get; set; } = DateTime.Now.ToString("yyMMddHHmmssfff");
    
    /// <summary>
    ///     创建时间
    /// </summary>
    public DateTime CreateTime { get; set; } = DateTime.Now;

    /// <summary>
    ///     更新时间
    /// </summary>
    public DateTime UpdateTime { get; set; } = DateTime.Now;
}