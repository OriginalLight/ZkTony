using System.Collections.Specialized;
using System.Web;
using Exposure.Contracts.Services;
using Microsoft.Windows.AppNotifications;

namespace Exposure.Services;

public class AppNotificationService : IAppNotificationService
{
    private readonly INavigationService _navigationService;

    public AppNotificationService(INavigationService navigationService)
    {
        _navigationService = navigationService;
    }

    public void Initialize()
    {
        AppNotificationManager.Default.NotificationInvoked += OnNotificationInvoked;

        AppNotificationManager.Default.Register();
    }

    public bool Show(string payload)
    {
        var appNotification = new AppNotification(payload);

        AppNotificationManager.Default.Show(appNotification);

        return appNotification.Id != 0;
    }

    public NameValueCollection ParseArguments(string arguments) => HttpUtility.ParseQueryString(arguments);

    public void Unregister() => AppNotificationManager.Default.Unregister();

    ~AppNotificationService()
    {
        Unregister();
    }

    public void OnNotificationInvoked(AppNotificationManager sender, AppNotificationActivatedEventArgs args) =>
        App.MainWindow.DispatcherQueue.TryEnqueue(() =>
        {
            App.MainWindow.BringToFront();
        });
}