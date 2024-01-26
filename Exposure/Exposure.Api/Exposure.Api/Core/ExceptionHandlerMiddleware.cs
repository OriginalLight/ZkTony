using Exposure.Api.Contracts.Services;

namespace Exposure.Api.Core;

public class ExceptionHandlerMiddleware
{
    private readonly IErrorLogService _errorLog;
    private readonly ILogger<ExceptionHandlerMiddleware> _logger;
    private readonly RequestDelegate _next;

    public ExceptionHandlerMiddleware(RequestDelegate next, IErrorLogService errorLog,
        ILogger<ExceptionHandlerMiddleware> logger)
    {
        _next = next;
        _logger = logger;
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
            _logger.LogError(ex, ex.Message);
            _errorLog.AddErrorLog(ex);
        }
    }
}