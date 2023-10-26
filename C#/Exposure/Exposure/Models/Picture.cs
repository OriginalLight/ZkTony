namespace Exposure.Models;

#pragma warning disable CS8618
public class Picture
{
    // 构造函数，用于创建新的 Picture 实例
    public Picture()
    {
        Id = Guid.NewGuid(); // 生成唯一标识符
    }

    // 图片的唯一标识符
    public Guid Id
    {
        get;
        private set;
    }

    // 图片的文件名
    public string Name
    {
        get;
        set;
    }

    // 图片的路径
    public string Path
    {
        get;
        set;
    }

    // 获取图片的全路径
    public string GetFullPath() =>
        // 拼接文件路径和文件名以获取完整路径
        System.IO.Path.Combine(Path, Name);
}
#pragma warning restore CS8618