﻿namespace Exposure.Api.Models.Dto;

public class PictureDto
{
    public long Id { get; set; }

    /// <summary>
    ///     用户
    /// </summary>
    public User User { get; set; }

    /// <summary>
    ///     图片名称
    /// </summary>
    public string Name { get; set; }

    /// <summary>
    ///     图片路径
    /// </summary>
    public string Path { get; set; }

    /// <summary>
    ///     宽度
    /// </summary>
    public int Width { get; set; }

    /// <summary>
    ///     高度
    /// </summary>
    public int Height { get; set; }

    /// <summary>
    ///     大小
    /// </summary>
    public double Size { get; set; }

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