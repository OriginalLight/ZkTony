using SqlSugar;

namespace Exposure.Api.Models;

[SugarTable("Picture")]
public class Picture
{
    [SugarColumn(IsPrimaryKey = true, IsIdentity = true)]
    public int Id { get; set; }

    /// <summary>
    ///     用户ID
    /// </summary>
    public int UserId { get; set; }

    /// <summary>
    ///     图片名称
    /// </summary>
    public string Name { get; set; }

    /// <summary>
    ///     图片路径
    /// </summary>
    public string Path { get; set; }
    
    /// <summary>
    ///     缩略图
    /// </summary>
    public string Thumbnail { get; set; }

    /// <summary>
    ///     宽度
    /// </summary>
    public int Width { get; set; }

    /// <summary>
    ///     高度
    /// </summary>
    public int Height { get; set; }

    public int Type { get; set; }

    /// <summary>
    ///     曝光时间
    /// </summary>
    public int ExposureTime { get; set; }

    /// <summary>
    ///     曝光增益
    /// </summary>
    public int ExposureGain { get; set; }

    /// <summary>
    ///     白平衡
    /// </summary>
    public int BlackLevel { get; set; }

    /// <summary>
    ///     是否删除
    /// </summary>
    public bool IsDelete { get; set; }

    /// <summary>
    ///     创建时间
    /// </summary>
    public DateTime CreateTime { get; set; }

    /// <summary>
    ///     更新时间
    /// </summary>
    public DateTime UpdateTime { get; set; }

    /// <summary>
    ///     删除时间
    /// </summary>
    public DateTime DeleteTime { get; set; }
}