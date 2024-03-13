using System.Text.Encodings.Web;
using System.Text.Json;
using Exposure.Api.Contracts.Services;
using Microsoft.AspNetCore.Mvc;

namespace Exposure.Api.Core.Exception;

public class ExceptionMiddleware(
    RequestDelegate next,
    IErrorLogService errorLog,
    ILogger<ExceptionMiddleware> logger)
{
    private readonly JsonSerializerOptions _jsonSerializerOptions = new()
    {
        // 驼峰
        PropertyNamingPolicy = JsonNamingPolicy.CamelCase,
        // Utf8 编码
        Encoder = JavaScriptEncoder.UnsafeRelaxedJsonEscaping
    };
    public async Task Invoke(HttpContext context)
    {
        try
        {
            await next(context);
        }
        catch (System.Exception ex)
        {
            context.Response.ContentType = "application/problem+json";
            context.Response.StatusCode = StatusCodes.Status500InternalServerError;
            
            logger.LogError(ex, ex.Message);
            errorLog.AddErrorLog(ex);
            var problemDetails = new ProblemDetails
            {
                Title = "An error occurred while processing your request.",
                Detail = ex.Message,
                Status = StatusCodes.Status500InternalServerError
            };
            await JsonSerializer.SerializeAsync(context.Response.Body, problemDetails, _jsonSerializerOptions);
        }
    }
}