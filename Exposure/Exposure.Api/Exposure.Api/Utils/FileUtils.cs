﻿using System.Reflection;

namespace Exposure.Api.Utils;

public class FileUtils
{
    #region 静态文件夹

    private static string MyPictures => Environment.GetFolderPath(Environment.SpecialFolder.MyPictures);
    
    public static string Exposure => Path.Combine(MyPictures, "Exposure");
    
    public static string Preview => Path.Combine(MyPictures, "Preview");
    
    public static string Thumbnail => Path.Combine(MyPictures, "Thumbnail");

    public static string Collect => Path.Combine(MyPictures, "Collect");
    
    public static string AppLocation => Path.GetDirectoryName(Assembly.GetEntryAssembly()?.Location) ?? throw new InvalidOperationException("无法获取程序集位置");

    #endregion
    
    #region 创建文件夹
    
    public static void CreateFolder(string folder)
    {
        if (!Directory.Exists(folder)) Directory.CreateDirectory(folder);
    }
    
    #endregion
    
    #region 删除文件夹
    
    public static void DeleteFolder(string folder)
    {
        if (Directory.Exists(folder)) Directory.Delete(folder, true);
    }
    
    #endregion
    
    #region 删除文件
    
    public static void DeleteFile(string file)
    {
        if (File.Exists(file)) File.Delete(file);
    }
    
    #endregion
    
    #region 复制文件

    public static void CopyFile(string source, string target)
    {
        if (!File.Exists(source)) throw new FileNotFoundException("源文件不存在", source);
        File.Copy(source, target, true);
    }
    
    #endregion
    
    #region 移动文件

    public static void MoveFile(string source, string target)
    {
        if (!File.Exists(source)) throw new FileNotFoundException("源文件不存在", source);
        File.Move(source, target);

    }
    
    #endregion
    
    #region 获取文件夹大小
    
    public static long GetFolderSize(string folder)
    {
        return !Directory.Exists(folder) ? 0 : Directory.GetFiles(folder, "*", SearchOption.AllDirectories).Sum(file => new FileInfo(file).Length);
    }
    
    #endregion
    
    #region 清空文件夹
    
    public static void ClearFolder(string folder)
    {
        if (!Directory.Exists(folder)) return;
        foreach (var file in Directory.GetFiles(folder))
        {
            File.Delete(file);
        }
    }
    
    #endregion
    
    #region 获取文件夹下所有文件
    
    public static List<string> GetFiles(string folder)
    {
        return Directory.GetFiles(folder).ToList();
    }
    
    #endregion

    #region 获取文件名

    public static string GetFileName(string folder, string name)
    {
        CreateFolder(folder);
        return Path.Combine(folder, name);
    }

    #endregion
    
}