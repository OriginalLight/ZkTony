namespace Exposure.Api.Models.Dto;

public class AlbumExportDto
{
    public int[] Ids { get; set; } = [];
    public string Format { get; set; } = string.Empty;
}