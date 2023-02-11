use std::sync::Arc;

use configs::CFG;
use once_cell::sync::Lazy;
use tracing::Level;
use tracing_subscriber::{
    fmt,
    fmt::format::{Compact, Format},
};

pub static RT: Lazy<Arc<tokio::runtime::Runtime>> = Lazy::new(|| {
    let rt = tokio::runtime::Runtime::new().unwrap();
    Arc::new(rt)
});

pub fn setup() {
    //   打印logo
    show_log();
}

fn show_log() {
    let logo = r#"
    ████████ ██     ██████████                                      ██     ███████  ██
    ░░░░░░██ ░██    ░░░░░██░░░                     ██   ██          ████   ░██░░░░██░██
         ██  ░██  ██    ░██      ██████  ███████  ░░██ ██          ██░░██  ░██   ░██░██
        ██   ░██ ██     ░██     ██░░░░██░░██░░░██  ░░███   █████  ██  ░░██ ░███████ ░██
       ██    ░████      ░██    ░██   ░██ ░██  ░██   ░██   ░░░░░  ██████████░██░░░░  ░██
      ██     ░██░██     ░██    ░██   ░██ ░██  ░██   ██          ░██░░░░░░██░██      ░██
     ████████░██░░██    ░██    ░░██████  ███  ░██  ██           ░██     ░██░██      ░██
    ░░░░░░░░ ░░  ░░     ░░      ░░░░░░  ░░░   ░░  ░░            ░░      ░░ ░░       ░░     
       "#;
    println!("{}", logo);
    println!("系统架构：{}", std::env::consts::OS);
    println!("系统类型：{}", std::env::consts::ARCH);
    println!("操作系统：{}", std::env::consts::FAMILY);
    println!()
}

pub fn get_log_level() -> Level {
    match CFG.log.log_level.as_str() {
        "TRACE" => Level::TRACE,
        "DEBUG" => Level::DEBUG,
        "INFO" => Level::INFO,
        "WARN" => Level::WARN,
        "ERROR" => Level::ERROR,
        _ => Level::INFO,
    }
}

#[cfg(any(target_os = "macos", target_os = "linux", target_os = "windows"))]
pub fn get_log_format() -> Format<Compact> {
    fmt::format()
        .with_level(true) // don't include levels in formatted output
        .with_target(true) // don't include targets
        .with_thread_ids(true)
        .with_thread_names(true)
        .with_file(true)
        .with_ansi(true)
        .with_line_number(true) // include the name of the current thread
        .compact()
}
