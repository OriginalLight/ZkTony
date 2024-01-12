using Exposure.Api.Contracts.Services;

namespace Exposure.Api.Core;

public class ExceptionHandlerMiddleware
{
    private readonly IErrorLogService _errorLog;
    private readonly RequestDelegate _next;

    public ExceptionHandlerMiddleware(RequestDelegate next, IErrorLogService errorLog)
    {
        _next = next;
        _errorLog = errorLog;
    }

    public async Task Invoke(HttpContext context)
    {
        try
        {
            await _next(context);
        }
        catch (Exception ex)
        {
            _errorLog.AddErrorLog(ex);
        }
    }
}