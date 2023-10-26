using System.Reflection;
using Windows.ApplicationModel;
using Exposure.Contracts.Services;
using Exposure.Helpers;

namespace Exposure.Services;

internal class AppInfoService : IAppInfoService
{
    public string GetAppNameLocalized() => "AppDisplayName".GetAppLocalized();

    public Version GetAppVersion()
    {
        if (!RuntimeHelper.IsMSIX)
        {
            return Assembly.GetExecutingAssembly().GetName().Version!;
        }

        var packageVersion = Package.Current.Id.Version;
        return new Version(packageVersion.Major, packageVersion.Minor, packageVersion.Build,
            packageVersion.Revision);
    }
}