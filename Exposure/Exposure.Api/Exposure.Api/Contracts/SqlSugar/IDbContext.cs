using SqlSugar;

namespace Exposure.Api.Contracts.SqlSugar;

public interface IDbContext
{
    /// <summary>
    ///     操作数据库对象
    /// </summary>
    public SqlSugarClient db { get; }

    /// <summary>
    ///     创建数据表
    /// </summary>
    /// <param name="Backup">是否备份</param>
    /// <param name="StringDefaultLength">string类型映射的长度</param>
    /// <param name="types">要创建的数据表</param>
    public void CreateTable(bool Backup = false, int StringDefaultLength = 50, params Type[] types);


    /// <summary>
    ///     创建表
    /// </summary>
    /// <param name="Backup">是否备份</param>
    /// <param name="StringDefaultLength">string类型映射的长度</param>
    public void CreateAllTable(bool Backup = false, int StringDefaultLength = 50);
}