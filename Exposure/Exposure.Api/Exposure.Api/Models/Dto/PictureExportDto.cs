namespace Exposure.Api.Models.Dto;

public class PictureExportDto
{
    public object[] Ids { get; set; } = Array.Empty<object>();
    public string Format { get; set; } = string.Empty;
}