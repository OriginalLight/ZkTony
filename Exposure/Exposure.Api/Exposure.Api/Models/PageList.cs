namespace Exposure.Api.Models;

public class PageList<T>
{
    public int Page { get; set; }
    public int Size { get; set; }
    public int Total { get; set; }
    public T Data { get; set; }
}