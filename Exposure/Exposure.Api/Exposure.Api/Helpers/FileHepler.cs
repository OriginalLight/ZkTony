using System.Text;

namespace Exposure.Api.Helpers;

public class FileHelper
{
    #region 检测指定路径是否存在

    /// <summary>
    ///     检测指定路径是否存在
    /// </summary>
    /// <param name="path">目录的绝对路径</param>
    public static bool IsExistDirectory(string path)
    {
        return Directory.Exists(path);
    }

    #endregion

    #region 检测指定文件是否存在,如果存在则返回true

    /// <summary>
    ///     检测指定文件是否存在,如果存在则返回true
    /// </summary>
    /// <param name="filePath">文件的绝对路径</param>
    public static bool IsExistFile(string filePath)
    {
        return File.Exists(filePath);
    }

    #endregion

    #region 创建文件夹

    /// <summary>
    ///     创建文件夹
    /// </summary>
    /// <param name="folderPath">文件夹的绝对路径</param>
    public static void CreateFolder(string folderPath)
    {
        if (!IsExistDirectory(folderPath)) Directory.CreateDirectory(folderPath);
    }

    #endregion

    #region 将文件移动到指定目录

    /// <summary>
    ///     将文件移动到指定目录
    /// </summary>
    /// <param name="sourceFilePath">需要移动的源文件的绝对路径</param>
    /// <param name="descDirectoryPath">移动到的目录的绝对路径</param>
    public static void Move(string sourceFilePath, string descDirectoryPath)
    {
        var sourceName = GetFileName(sourceFilePath);
        if (IsExistDirectory(descDirectoryPath))
        {
            //如果目标中存在同名文件,则删除
            if (IsExistFile(descDirectoryPath + "\\" + sourceFilePath))
                DeleteFile(descDirectoryPath + "\\" + sourceFilePath);
            else
                //将文件移动到指定目录
                File.Move(sourceFilePath, descDirectoryPath + "\\" + sourceFilePath);
        }
    }

    #endregion

    # region 将源文件的内容复制到目标文件中

    /// <summary>
    ///     将源文件的内容复制到目标文件中
    /// </summary>
    /// <param name="sourceFilePath">源文件的绝对路径</param>
    /// <param name="descDirectoryPath">目标文件的绝对路径</param>
    public static void Copy(string sourceFilePath, string descDirectoryPath)
    {
        File.Copy(sourceFilePath, descDirectoryPath, true);
    }

    #endregion

    #region 从文件的绝对路径中获取文件名( 不包含扩展名 )

    /// <summary>
    ///     从文件的绝对路径中获取文件名( 不包含扩展名 )
    /// </summary>
    /// <param name="filePath">文件的绝对路径</param>
    public static string GetFileName(string filePath)
    {
        var file = new FileInfo(filePath);
        return file.Name;
    }

    #endregion

    #region 删除指定文件

    /// <summary>
    ///     删除指定文件
    /// </summary>
    /// <param name="filePath">文件的绝对路径</param>
    public static void DeleteFile(string filePath)
    {
        if (IsExistFile(filePath)) File.Delete(filePath);
    }

    #endregion

    #region 删除指定目录及其所有子目录

    /// <summary>
    ///     删除指定目录及其所有子目录
    /// </summary>
    /// <param name="directoryPath">文件的绝对路径</param>
    public static void DeleteDirectory(string directoryPath)
    {
        if (IsExistDirectory(directoryPath)) Directory.Delete(directoryPath);
    }

    #endregion

    #region 清空指定目录下所有文件及子目录,但该目录依然保存.

    /// <summary>
    ///     清空指定目录下所有文件及子目录,但该目录依然保存.
    /// </summary>
    /// <param name="directoryPath">指定目录的绝对路径</param>
    public static void ClearDirectory(string directoryPath)
    {
        if (!IsExistDirectory(directoryPath)) return;
        //删除目录中所有的文件
        var fileNames = GetFileNames(directoryPath);
        for (var i = 0; i < fileNames.Length; i++) DeleteFile(fileNames[i]);

        //删除目录中所有的子目录
        var directoryNames = GetDirectories(directoryPath);
        for (var i = 0; i < directoryNames.Length; i++) DeleteDirectory(directoryNames[i]);
    }

    #endregion

    #region 剪切  粘贴

    /// <summary>
    ///     剪切文件
    /// </summary>
    /// <param name="source">原路径</param>
    /// <param name="destination">新路径</param>
    public bool FileMove(string source, string destination)
    {
        var ret = false;
        var file_s = new FileInfo(source);
        var file_d = new FileInfo(destination);
        if (file_s.Exists)
            if (!file_d.Exists)
            {
                file_s.MoveTo(destination);
                ret = true;
            }

        if (ret)
        {
            //Response.Write("<script>alert('剪切文件成功！');</script>");
        }

        //Response.Write("<script>alert('剪切文件失败！');</script>");
        return ret;
    }

    #endregion

    #region 检测指定目录是否为空

    /// <summary>
    ///     检测指定目录是否为空
    /// </summary>
    /// <param name="directoryPath">指定目录的绝对路径</param>
    public static bool IsEmptyDirectory(string directoryPath)
    {
        try
        {
            //判断文件是否存在
            var fileNames = GetFileNames(directoryPath);
            if (fileNames.Length > 0) return false;

            //判断是否存在文件夹
            var directoryNames = GetDirectories(directoryPath);
            if (directoryNames.Length > 0) return false;

            return true;
        }
        catch (Exception ex)
        {
            return true;
        }
    }

    #endregion

    #region 获取指定目录中所有文件列表

    /// <summary>
    ///     获取指定目录中所有文件列表
    /// </summary>
    /// <param name="directoryPath">指定目录的绝对路径</param>
    public static string[] GetFileNames(string directoryPath)
    {
        if (!IsExistDirectory(directoryPath)) throw new FileNotFoundException();

        return Directory.GetFiles(directoryPath);
    }

    #endregion

    #region 创建文件夹

    /// <summary>
    ///     创建文件夹
    /// </summary>
    /// <param name="fileName">文件的绝对路径</param>
    public static void CreateSuffic(string fileName)
    {
        if (!Directory.Exists(fileName)) Directory.CreateDirectory(fileName);
    }

    /// <summary>
    ///     创建文件夹
    /// </summary>
    /// <param name="fileName">文件的绝对路径</param>
    public static void CreateFiles(string fileName)
    {
        try
        {
            //判断文件是否存在，不存在创建该文件
            if (!IsExistFile(fileName))
            {
                var file = new FileInfo(fileName);
                var fs = file.Create();
                fs.Close();
            }
        }
        catch (Exception ex)
        {
            throw ex;
        }
    }

    /// <summary>
    ///     创建一个文件,并将字节流写入文件。
    /// </summary>
    /// <param name="filePath">文件的绝对路径</param>
    /// <param name="buffer">二进制流数据</param>
    public static void CreateFile(string filePath, byte[] buffer)
    {
        try
        {
            //判断文件是否存在，不存在创建该文件
            if (!IsExistFile(filePath))
            {
                var file = new FileInfo(filePath);
                var fs = file.Create();
                fs.Write(buffer, 0, buffer.Length);
                fs.Close();
            }
            else
            {
                File.WriteAllBytes(filePath, buffer);
            }
        }
        catch (Exception ex)
        {
            throw ex;
        }
    }

    #endregion

    #region 获取文件的后缀名

    /// <summary>
    ///     获取文件的后缀名
    /// </summary>
    /// <param name="filePath">文件的绝对路径</param>
    public static string GetExtension(string filePath)
    {
        var file = new FileInfo(filePath);
        return file.Extension;
    }

    /// <summary>
    ///     返回文件扩展名，不含“.”
    /// </summary>
    /// <param name="filepath">文件全名称</param>
    /// <returns>string</returns>
    public static string GetFileExt(string filepath)
    {
        if (string.IsNullOrEmpty(filepath)) return "";

        if (filepath.LastIndexOf(".", StringComparison.Ordinal) > 0)
            return filepath.Substring(filepath.LastIndexOf(".", StringComparison.Ordinal) + 1); //文件扩展名，不含“.”

        return "";
    }

    #endregion

    #region 获取指定目录中的子目录列表

    /// <summary>
    ///     获取指定目录中所有子目录列表,若要搜索嵌套的子目录列表,请使用重载方法
    /// </summary>
    /// <param name="directoryPath">指定目录的绝对路径</param>
    public static string[] GetDirectories(string directoryPath)
    {
        try
        {
            return Directory.GetDirectories(directoryPath);
        }
        catch (Exception ex)
        {
            throw ex;
        }
    }

    /// <summary>
    ///     获取指定目录及子目录中所有子目录列表
    /// </summary>
    /// <param name="directoryPath">指定目录的绝对路径</param>
    /// <param name="searchPattern">
    ///     模式字符串，"*"代表0或N个字符，"?"代表1个字符。
    ///     范例："Log*.xml"表示搜索所有以Log开头的Xml文件。
    /// </param>
    /// <param name="isSearchChild">是否搜索子目录</param>
    public static string[] GetDirectories(string directoryPath, string searchPattern, bool isSearchChild)
    {
        try
        {
            if (isSearchChild)
                return Directory.GetDirectories(directoryPath, searchPattern, SearchOption.AllDirectories);
            return Directory.GetDirectories(directoryPath, searchPattern, SearchOption.TopDirectoryOnly);
        }
        catch (Exception ex)
        {
            throw ex;
        }
    }

    #endregion

    #region 获取一个文件的长度

    /// <summary>
    ///     获取一个文件的长度,单位为Byte
    /// </summary>
    /// <param name="filePath">文件的绝对路径</param>
    public static int GetFileSize(string filePath)
    {
        //创建一个文件对象 
        var fi = new FileInfo(filePath);
        //获取文件的大小 
        return (int)fi.Length;
    }

    /// <summary>
    ///     获取一个文件的长度,单位为KB
    /// </summary>
    /// <param name="filePath">文件的路径</param>
    public static double GetFileSizeByKb(string filePath)
    {
        //创建一个文件对象 
        var fi = new FileInfo(filePath);
        //获取文件的大小 
        return
            Math.Round(Convert.ToDouble(filePath.Length) / 1024,
                2); // ConvertHelper.ToDouble(ConvertHelper.ToDouble(fi.Length) / 1024, 1);
    }

    /// <summary>
    ///     获取一个文件的长度,单位为MB
    /// </summary>
    /// <param name="filePath">文件的路径</param>
    public static double GetFileSizeByMb(string filePath)
    {
        //创建一个文件对象 
        var fi = new FileInfo(filePath);
        //获取文件的大小 
        return Math.Round(Convert.ToDouble(Convert.ToDouble(fi.Length) / 1024 / 1024), 2);
    }

    #endregion

    #region 将文件读取到字符串中

    /// <summary>
    ///     将文件读取到字符串中
    /// </summary>
    /// <param name="filePath">文件的绝对路径</param>
    public static string FileToString(string filePath)
    {
        return FileToString(filePath, Encoding.UTF8);
    }

    /// <summary>
    ///     将文件读取到字符串中
    /// </summary>
    /// <param name="filePath">文件的绝对路径</param>
    /// <param name="encoding">字符编码</param>
    public static string FileToString(string filePath, Encoding encoding)
    {
        //创建流读取器
        var reader = new StreamReader(filePath, encoding);
        try
        {
            //读取流
            return reader.ReadToEnd();
        }
        catch (Exception ex)
        {
            throw ex;
        }
        finally
        {
            //关闭流读取器
            reader.Close();
        }
    }

    #endregion
}