using SqlSugar;

namespace Exposure.Api.Models;

[SugarTable("Photo")]
public class Photo
{
    [SugarColumn(IsPrimaryKey = true, IsIdentity = true)]
    public int Id { get; set; }

    /// <summary>
    ///     图集ID
    /// </summary>
    public int AlbumId { get; set; }

    /// <summary>
    ///     图片名称
    /// </summary>
    public string Name { get; set; } = string.Empty;

    /// <summary>
    ///     图片路径
    /// </summary>
    public string Path { get; set; } = string.Empty;

    /// <summary>
    ///     缩略图
    /// </summary>
    public string Thumbnail { get; set; } = string.Empty;

    /// <summary>
    ///     宽度
    /// </summary>
    public int Width { get; set; }

    /// <summary>
    ///     高度
    /// </summary>
    public int Height { get; set; }

    /// <summary>
    ///     图片类型 -2: 原始图片 -1:预览图 0:白光 1：曝光 2：合成
    /// </summary>
    public int Type { get; set; }

    /// <summary>
    ///     曝光时间
    /// </summary>
    public int ExposureTime { get; set; }

    /// <summary>
    ///     增益
    /// </summary>
    public int Gain { get; set; }
    
    /// <summary>
    ///     创建时间
    /// </summary>
    public DateTime CreateTime { get; set; } = DateTime.Now;
    
}