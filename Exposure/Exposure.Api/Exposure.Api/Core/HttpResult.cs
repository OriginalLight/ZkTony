namespace Exposure.Api.Core;

public class HttpResult
{
    public HttpResult(int code, string? msg, object? data)
    {
        Code = code;
        Msg = msg;
        Data = data;
    }

    public int Code { get; set; }
    public string? Msg { get; set; }
    public object? Data { get; set; }

    public static HttpResult Success()
    {
        return new HttpResult((int)ResultCode.Success, "请求成功", null);
    }

    public static HttpResult Success(object? data)
    {
        return new HttpResult((int)ResultCode.Success, "请求成功", data);
    }

    public static HttpResult Success(string? msg, object? data)
    {
        return new HttpResult((int)ResultCode.Success, msg, data);
    }

    public static HttpResult Fail(string? msg)
    {
        return new HttpResult((int)ResultCode.Fail, msg, null);
    }

    public static HttpResult Fail(string? msg, object? data)
    {
        return new HttpResult((int)ResultCode.Fail, msg, data);
    }

    public static HttpResult Fail(ResultCode code, string? msg)
    {
        return new HttpResult((int)code, msg, null);
    }

    public static HttpResult Fail(ResultCode code, string? msg, object? data)
    {
        return new HttpResult((int)code, msg, data);
    }

    public static HttpResult Fail(Exception ex)
    {
        return new HttpResult((int)ResultCode.Fail, ex.Message, null);
    }
}