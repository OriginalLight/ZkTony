using System.Reflection;
using Exposure.Api.Contracts.SqlSugar;
using SqlSugar;

namespace Exposure.Api.Core;

/// <summary>
///     数据库上下文
/// </summary>
public class AppDbContext : IDbContext
{
    private readonly IConfiguration Configuration;
    private readonly ILogger<AppDbContext> _logger;

    public AppDbContext(IConfiguration configuration, ILogger<AppDbContext> logger)
    {
        _logger = logger;
        Configuration = configuration;
        //打印日志
        db.Aop.OnLogExecuting = (sql, paramster) =>
        {
            _logger.LogInformation(sql + "\r\n" +
                              $"{db.Utilities.SerializeObject(paramster.ToDictionary(it => it.ParameterName, it => it.Value))} \r\n");
        };
    }

    public AppDbContext()
    {
    }

    /// <summary>
    ///     操作数据库对象
    /// </summary>
    public SqlSugarClient db =>
        new(new ConnectionConfig
        {
            ConnectionString =
                $"Data Source={Path.Combine(Path.GetDirectoryName(Assembly.GetEntryAssembly().Location), "Exposure.db")}",
            DbType = DbType.Sqlite, //数据库类型
            IsAutoCloseConnection = true, //自动释放数据务，如果存在事务，在事务结束后释放
            LanguageType = LanguageType.Chinese,
            InitKeyType = InitKeyType.Attribute //从实体特性中读取主键自增列信息
        });


    /// <summary>
    ///     创建数据表
    /// </summary>
    /// <param name="Backup">是否备份</param>
    /// <param name="StringDefaultLength">string类型映射的长度</param>
    /// <param name="types">要创建的数据表</param>
    public void CreateTable(bool Backup = false, int StringDefaultLength = 50, params Type[] types)
    {
        //设置varchar的默认长度
        db.CodeFirst.SetStringDefaultLength(StringDefaultLength);

        //创建表
        if (Backup)
            db.CodeFirst.BackupTable().InitTables(types);
        else
            db.CodeFirst.InitTables(types);
    }

    /// <summary>
    ///     创建表
    /// </summary>
    /// <param name="Backup">是否备份</param>
    /// <param name="StringDefaultLength">string类型映射的长度</param>
    public void CreateAllTable(bool Backup = false, int StringDefaultLength = 50)
    {
        //设置varchar的默认长度
        db.CodeFirst.SetStringDefaultLength(StringDefaultLength);

        var assembly = Assembly.Load("SqlSugar.Model");
        var types = assembly.GetTypes().Where(t => t.FullName.Contains("Models")).ToArray();

        var b = db.DbMaintenance.CreateDatabase();
        //创建表
        if (Backup)
            db.CodeFirst.BackupTable().InitTables(types);
        else
            db.CodeFirst.InitTables(types);
    }
}