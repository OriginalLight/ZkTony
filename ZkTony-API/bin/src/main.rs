// use std::time::Duration;

use std::{net::SocketAddr, str::FromStr};

//
use axum::Router;
use configs::CFG;
use tokio::signal;
use tracing_subscriber::{fmt, layer::SubscriberExt, EnvFilter, Registry};
use utils::my_env::{self, RT};
// 路由日志追踪

// #[tokio::main]
fn main() {
    RT.block_on(async {
        if std::env::var_os("RUST_LOG").is_none() {
            std::env::set_var("RUST_LOG", &CFG.log.log_level);
        }
        my_env::setup();

        // 系统变量设置
        let log_env = my_env::get_log_level();

        //  日志设置
        let format = my_env::get_log_format();

        let file_appender = tracing_appender::rolling::hourly(&CFG.log.dir, &CFG.log.file);
        let (non_blocking, _guard) = tracing_appender::non_blocking(file_appender);

        // 标准控制台输出
        let (std_non_blocking, _guard) = tracing_appender::non_blocking(std::io::stdout());
        let logger = Registry::default()
            .with(EnvFilter::from_default_env().add_directive(log_env.into()))
            .with(fmt::Layer::default().with_writer(std_non_blocking).event_format(format.clone()).pretty())
            .with(fmt::Layer::default().with_writer(non_blocking).event_format(format))
            // .with(console_layer)
            ;
        tracing::subscriber::set_global_default(logger).unwrap();


        let addr = SocketAddr::from_str(&CFG.server.address).unwrap();
        tracing::info!("Server listening on {}", addr);

        let app = Router::new()
            //  "/" 与所有路由冲突
            .nest("/", api::api());

        axum::Server::bind(&addr)
                .serve(app.into_make_service())
                .with_graceful_shutdown(shutdown_signal())
                .await
                .unwrap()
    })
}

async fn shutdown_signal() {
    let ctrl_c = async {
        signal::ctrl_c()
            .await
            .expect("failed to install Ctrl+C handler");
    };

    #[cfg(unix)]
    let _terminate = async {
        signal::unix::signal(signal::unix::SignalKind::terminate())
            .expect("failed to install signal handler")
            .recv()
            .await;
    };

    #[cfg(not(target_os = "unix"))]
    let _terminate = std::future::pending::<()>();

    tokio::select! {
        _ = ctrl_c => {},
        _ = _terminate => {},
    }

    println!("signal received, starting graceful shutdown");
}
