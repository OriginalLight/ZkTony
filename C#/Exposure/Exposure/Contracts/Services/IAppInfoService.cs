using System;

namespace Exposure.Contracts.Services;
public interface IAppInfoService
{
    public string GetAppNameLocalized();

    public Version GetAppVersion();
}
