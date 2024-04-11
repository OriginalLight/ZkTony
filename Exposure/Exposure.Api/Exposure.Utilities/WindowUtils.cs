using System.Runtime.InteropServices;

namespace Exposure.Utilities;

public static class WindowUtils
{
    private const int SW_HIDE = 0;
    private const int SW_SHOW = 5;

    public static void Hide()
    {
        ShowWindow(GetConsoleWindow(), SW_HIDE);
    }

    [DllImport("kernel32.dll")]
    private static extern IntPtr GetConsoleWindow();

    [DllImport("user32.dll")]
    private static extern bool ShowWindow(IntPtr hWnd, int nCmdShow);
}