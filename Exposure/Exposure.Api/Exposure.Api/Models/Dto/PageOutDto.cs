namespace Exposure.Api.Models.Dto;

public class PageOutDto<T>
{
    public int Total { get; set; }
    public T? List { get; set; }
}