namespace Exposure.Api.Models.Dto;

public class PageOutDto<T>
{
    public int Page { get; set; }
    public int Size { get; set; }
    public int Total { get; set; }
    public T? Data { get; set; }
}