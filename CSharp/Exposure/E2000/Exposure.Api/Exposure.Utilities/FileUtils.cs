using System.Reflection;

namespace Exposure.Utilities;

public static class FileUtils
{
    #region 静态文件夹

    private static string MyPictures => Environment.GetFolderPath(Environment.SpecialFolder.MyPictures);
    
    public static string Original => Directory.Exists(Path.Combine(MyPictures, "Original")) ? Path.Combine(MyPictures, "Original") : Directory.CreateDirectory(Path.Combine(MyPictures, "Original")).FullName;

    public static string Exposure => Directory.Exists(Path.Combine(MyPictures, "Exposure")) ? Path.Combine(MyPictures, "Exposure") : Directory.CreateDirectory(Path.Combine(MyPictures, "Exposure")).FullName;

    public static string Preview => Directory.Exists(Path.Combine(MyPictures, "Preview")) ? Path.Combine(MyPictures, "Preview") : Directory.CreateDirectory(Path.Combine(MyPictures, "Preview")).FullName;

    public static string Thumbnail => Directory.Exists(Path.Combine(MyPictures, "Thumbnail")) ? Path.Combine(MyPictures, "Thumbnail") : Directory.CreateDirectory(Path.Combine(MyPictures, "Thumbnail")).FullName;

    public static string Collect => Directory.Exists(Path.Combine(MyPictures, "Collect")) ? Path.Combine(MyPictures, "Collect") : Directory.CreateDirectory(Path.Combine(MyPictures, "Collect")).FullName;

    public static string Calibration => Directory.Exists(Path.Combine(MyPictures, "Calibration")) ? Path.Combine(MyPictures, "Calibration") : Directory.CreateDirectory(Path.Combine(MyPictures, "Calibration")).FullName;

    public static string AppLocation => Path.GetDirectoryName(Assembly.GetEntryAssembly()?.Location) ??
                                        throw new InvalidOperationException("无法获取程序集位置");

    #endregion
}