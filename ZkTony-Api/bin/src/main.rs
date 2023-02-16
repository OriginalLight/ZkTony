use axum::{http::Method, middleware, response::Html, Router};
use axum_server::tls_rustls::RustlsConfig;
use configs::CFG;
use metrics::layer::track_metrics;
use std::{net::SocketAddr, str::FromStr};
use tokio::signal;
use tower_http::{
    compression::{predicate::NotForContentType, CompressionLayer, DefaultPredicate, Predicate},
    cors::{Any, CorsLayer},
};
use tracing_subscriber::{fmt, layer::SubscriberExt, EnvFilter, Registry};
use utils::my_env::{self, RT};

// region: main_app
fn main_app() -> Router {
    Router::new().nest("/", api::api())
}
// endregion

// region: metrics_app
fn metrics_app() -> Router {
    Router::new().nest("/metrics", metrics::api())
}
// endregion

// region: start_main_server
async fn start_main_server() {
    let app = main_app();
    //  跨域
    let cors = CorsLayer::new()
        .allow_methods(vec![Method::GET, Method::POST, Method::PUT, Method::DELETE])
        .allow_origin(Any)
        .allow_headers(Any);
    let app = app.layer(cors);
    //  压缩
    let app = match &CFG.server.content_gzip {
        true => {
            //  开启压缩后 SSE 数据无法返回  text/event-stream 单独处理不压缩
            let predicate =
                DefaultPredicate::new().and(NotForContentType::new("text/event-stream"));
            app.layer(CompressionLayer::new().compress_when(predicate))
        }
        false => app,
    };
    let app = match &CFG.server.metrics {
        true => app.route_layer(middleware::from_fn(track_metrics)),
        false => app,
    };
    let app = app.fallback(|| async { Html("<h1>404 Not Found</h1>") });
    let addr = SocketAddr::from_str(&CFG.server.address).unwrap();
    tracing::info!("Server listening on {}", addr);
    // ssl
    match CFG.server.ssl {
        true => {
            let config = RustlsConfig::from_pem_file(&CFG.cert.cert, &CFG.cert.key)
                .await
                .unwrap();
            axum_server::bind_rustls(addr, config)
                .serve(app.into_make_service())
                .await
                .unwrap()
        }

        false => axum::Server::bind(&addr)
            .serve(app.into_make_service())
            .with_graceful_shutdown(shutdown_signal())
            .await
            .unwrap(),
    }
}
// endregion

// region: start_metrics_server
async fn start_metrics_server() {
    let app = metrics_app();
    let addr = SocketAddr::from_str(&CFG.metrics.address).unwrap();
    tracing::info!("Metrics server listening on {}", addr);
    match CFG.server.ssl {
        true => {
            let config = RustlsConfig::from_pem_file(&CFG.cert.cert, &CFG.cert.key)
                .await
                .unwrap();
            axum_server::bind_rustls(addr, config)
                .serve(app.into_make_service())
                .await
                .unwrap()
        }

        false => axum::Server::bind(&addr)
            .serve(app.into_make_service())
            .with_graceful_shutdown(shutdown_signal())
            .await
            .unwrap(),
    }
}
// endregion

// region: main
fn main() {
    RT.block_on(async {
        if std::env::var_os("RUST_LOG").is_none() {
            std::env::set_var("RUST_LOG", &CFG.log.log_level);
        }
        my_env::setup();

        // 系统变量设置
        let log_env = my_env::get_log_level();

        // 格式化输出
        let format = my_env::get_log_format();

        // 文件输出
        let file_appender = tracing_appender::rolling::hourly(&CFG.log.dir, &CFG.log.file);
        let (non_blocking, _guard) = tracing_appender::non_blocking(file_appender);

        // 标准控制台输出
        let (std_non_blocking, _guard) = tracing_appender::non_blocking(std::io::stdout());
        let logger = Registry::default()
            .with(EnvFilter::from_default_env().add_directive(log_env.into()))
            .with(
                fmt::Layer::default()
                    .with_writer(std_non_blocking)
                    .event_format(format.clone())
                    .pretty(),
            )
            .with(
                fmt::Layer::default()
                    .with_writer(non_blocking)
                    .event_format(format),
            );

        tracing::subscriber::set_global_default(logger).unwrap();
        if CFG.server.metrics {
            tokio::join!(start_main_server(), start_metrics_server());
        } else {
            start_main_server().await;
        }
    })
}
// endregion

// region: shutdown_signal
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
// endregion
