namespace Exposure.Api.Models.Dto;

public class AlbumOutDto
{
    /// <summary>
    ///     主键
    /// </summary>
    public int Id { get; set; }

    /// <summary>
    ///     用户ID
    /// </summary>
    public User? User { get; set; }

    /// <summary>
    ///     图片名称
    /// </summary>
    public string Name { get; set; } = String.Empty;

    /// <summary>
    ///     图片
    /// </summary>
    public List<Photo> Photos { get; set; } = new();
    
    /// <summary>
    ///     原始曝光图
    /// </summary>
    public List<Photo> Original { get; set; } = new();
    
    /// <summary>
    ///     创建时间
    /// </summary>
    public DateTime CreateTime { get; set; }

    /// <summary>
    ///     更新时间
    /// </summary>
    public DateTime UpdateTime { get; set; }
}