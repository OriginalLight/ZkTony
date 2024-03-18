using SqlSugar;

namespace Exposure.Api.Models;

[SugarTable("Option")]
public class Option
{
    /// <summary>
    ///     主键
    /// </summary>
    [SugarColumn(IsPrimaryKey = true, IsIdentity = true)]
    public int Id { get; set; }
    
    /// <summary>
    ///     键
    /// </summary>
    public string Key { get; set; } = string.Empty;
    
    /// <summary>
    ///     值
    /// </summary>
    public string Value { get; set; } = string.Empty;
    
    /// <summary>
    ///     描述
    /// </summary>
    public string Description { get; set; } = string.Empty;
}