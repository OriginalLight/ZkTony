use serde::Deserialize;

/// 配置文件
#[derive(Debug, Deserialize)]
pub struct Configs {
    pub database: Database,
    pub server: Server,
    pub metrics: Metrics,
    pub log: Log,
}

// metrics 配置文件
#[derive(Debug, Deserialize)]
pub struct Metrics {
    pub address: String,
}

/// server 配置文件
#[derive(Debug, Deserialize)]
pub struct Server {
    pub address: String,
}

/// 日志配置
#[derive(Debug, Deserialize)]
pub struct Log {
    /// `log_level` 日志输出等级
    pub log_level: String,
    /// `dir` 日志输出文件夹
    pub dir: String,
    /// `file` 日志输出文件名
    pub file: String,
    /// 允许操作日志输出
    pub enable_oper_log: bool,
}

/// 数据库
#[derive(Debug, Deserialize)]
pub struct Database {
    /// 数据库连接
    pub link: String,
}
