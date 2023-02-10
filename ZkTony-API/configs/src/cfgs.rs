use serde::Deserialize;

/// 配置文件
#[derive(Debug, Deserialize)]
pub struct Configs {
    /// 程序配置
    pub server: Server,
    pub database: Database,
}

/// server 配置文件
#[derive(Debug, Deserialize)]
pub struct Server {
    pub address: String,
}


/// 数据库
#[derive(Debug, Deserialize)]
pub struct Database {
    /// 数据库连接
    pub link: String,
}