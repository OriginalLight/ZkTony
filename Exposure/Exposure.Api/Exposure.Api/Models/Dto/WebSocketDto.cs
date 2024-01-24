namespace Exposure.Api.Models.Dto;

public class WebSocketDto
{
    public string Code { get; set; } = string.Empty;
    public Dictionary<string, Object>? Data { get; set; }
}