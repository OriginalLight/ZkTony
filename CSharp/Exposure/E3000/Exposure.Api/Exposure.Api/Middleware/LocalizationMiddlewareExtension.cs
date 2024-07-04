using System.Globalization;
using Microsoft.AspNetCore.Localization;

namespace Exposure.Api.Middleware;

public static class LocalizationMiddlewareExtension
{
    /// <summary>
    ///     静态方法
    /// </summary>
    /// <param name="app">要进行扩展的类型</param>
    public static void UseLocalizationMiddleware(this IApplicationBuilder app)
    {
        app.UseRequestLocalization(options =>
        {
            var supportedCultures = new List<CultureInfo>
            {
                new("en"),
                new("zh")
            };
            options.DefaultRequestCulture = new RequestCulture("zh");
            options.SupportedCultures = supportedCultures;
            options.SupportedUICultures = supportedCultures;
        });
    }
}