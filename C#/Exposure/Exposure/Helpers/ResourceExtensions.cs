using Microsoft.Windows.ApplicationModel.Resources;

namespace Exposure.Helpers;

public static class ResourceExtensions
{
    private static readonly ResourceLoader _resourceLoader = new();

    public static string GetAppLocalized(this string resourceKey) => _resourceLoader.GetString(resourceKey);
}