use std::{fs::File, io::Read};

use once_cell::sync::Lazy;
use serde::Deserialize;

/// 配置文件
#[derive(Debug, Deserialize)]
pub struct Configs {
    pub cert: Cert,
    pub database: Database,
    pub server: Server,
    pub log: Log,
}

/// server 配置文件
#[derive(Debug, Deserialize)]
pub struct Server {
    pub name: String,
    pub version: String,
    pub addr: Vec<String>,
    pub ssl: bool,
    pub content_gzip: bool,
}

// cert 配置文件
#[derive(Debug, Deserialize)]
pub struct Cert {
    pub cert: String,
    pub key: String,
}

/// 日志配置
#[derive(Debug, Deserialize)]
pub struct Log {
    /// `log_level` 日志输出等级
    pub log_level: String,
}

/// 数据库
#[derive(Debug, Deserialize)]
pub struct Database {
    /// 数据库连接
    pub link: String,
}

const CFG_FILE: &str = "env/env.toml";
//  只要是配置文件中的配置项，都可以通过这个结构体来获取，
// 只要读取一次值后保存到内存，一直可供使用
pub static CFG: Lazy<Configs> = Lazy::new(self::Configs::init);

impl Configs {
    pub fn init() -> Self {
        let mut file = match File::open(CFG_FILE) {
            Ok(f) => f,
            Err(e) => panic!("不存在配置文件：{}，错误信息：{}", CFG_FILE, e),
        };
        let mut cfg_contents = String::new();
        match file.read_to_string(&mut cfg_contents) {
            Ok(s) => s,
            Err(e) => panic!("读取配置文件失败，错误信息：{}", e),
        };
        toml::from_str(&cfg_contents).expect("解析配置文件错误")
    }
}
