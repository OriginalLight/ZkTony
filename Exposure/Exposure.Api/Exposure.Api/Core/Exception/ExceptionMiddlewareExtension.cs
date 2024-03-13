namespace Exposure.Api.Core.Exception;

public static class ExceptionMiddlewareExtension
{
    /// <summary>
    /// 静态方法
    /// </summary>
    /// <param name="app">要进行扩展的类型</param>
    public static void UseExceptionMiddleware(this IApplicationBuilder app)
    {
        app.UseMiddleware(typeof(ExceptionMiddleware));
    }
}