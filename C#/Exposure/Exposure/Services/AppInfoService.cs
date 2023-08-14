using System.Reflection;
using Exposure.Contracts.Services;
using Exposure.Helpers;
using Windows.ApplicationModel;

namespace Exposure.Services;
internal class AppInfoService : IAppInfoService
{
    public string GetAppNameLocalized()
    {
        return "AppDisplayName".GetLocalized();
    }
    public Version GetAppVersion()
    {
        if (RuntimeHelper.IsMSIX)
        {
            var packageVersion = Package.Current.Id.Version;
            return new(packageVersion.Major, packageVersion.Minor, packageVersion.Build, packageVersion.Revision);
        }
        else
        {
            return Assembly.GetExecutingAssembly().GetName().Version!;
        }
    }
}
