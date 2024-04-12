using System.Reflection;
using Exposure.SqlSugar.Contracts;
using Exposure.Utilities;
using Microsoft.Extensions.Configuration;
using Serilog;
using SqlSugar;

namespace Exposure.SqlSugar;

/// <summary>
///     数据库上下文
/// </summary>
public class AppDbContext : IDbContext
{
    private readonly IConfiguration _config;

    public AppDbContext(IConfiguration config)
    {
        _config = config;

        var dataBase = _config.GetSection("DataBase").Value ?? throw new InvalidOperationException("无法获取数据库名称");
        //打印日志
        db = new SqlSugarClient(new ConnectionConfig
        {
            ConnectionString = $"Data Source={Path.Combine(FileUtils.AppLocation, dataBase)}",
            DbType = DbType.Sqlite, //数据库类型
            IsAutoCloseConnection = true, //自动释放数据务，如果存在事务，在事务结束后释放
            LanguageType = LanguageType.Chinese,
            InitKeyType = InitKeyType.Attribute //从实体特性中读取主键自增列信息
        });

        // Dev环境打印sql
        if (Environment.GetEnvironmentVariable("ASPNETCORE_ENVIRONMENT") == "Development")
            db.Aop.OnLogExecuting = (sql, paramster) =>
            {
                Log.Information(sql + "\r\n" +
                                       $"{db.Utilities.SerializeObject(paramster.ToDictionary(it => it.ParameterName, it => it.Value))} \r\n");
            };
    }


    public SqlSugarClient db { get; }

    /// <summary>
    ///     创建数据表
    /// </summary>
    /// <param name="backup">是否备份</param>
    /// <param name="stringDefaultLength">string类型映射的长度</param>
    /// <param name="types">要创建的数据表</param>
    public void CreateTable(bool backup = false, int stringDefaultLength = 50, params Type[] types)
    {
        //设置varchar的默认长度
        db.CodeFirst.SetStringDefaultLength(stringDefaultLength);

        //创建表
        if (backup)
            db.CodeFirst.BackupTable().InitTables(types);
        else
            db.CodeFirst.InitTables(types);
    }

    /// <summary>
    ///     创建表
    /// </summary>
    /// <param name="backup">是否备份</param>
    /// <param name="stringDefaultLength">string类型映射的长度</param>
    public void CreateAllTable(bool backup = false, int stringDefaultLength = 50)
    {
        //设置varchar的默认长度
        db.CodeFirst.SetStringDefaultLength(stringDefaultLength);

        var assembly = Assembly.Load("SqlSugar.Model");
        var types = assembly.GetTypes().Where(t => t.FullName != null && t.FullName.Contains("Models")).ToArray();

        db.DbMaintenance.CreateDatabase();
        //创建表
        if (backup)
            db.CodeFirst.BackupTable().InitTables(types);
        else
            db.CodeFirst.InitTables(types);
    }
}