use grpc_api::{health::health_svc, sea_orm::Database, service::ServerExt, CFG};
use std::{net::SocketAddr, str::FromStr};
use tokio::{signal, sync::mpsc};
use tonic::transport::Server;

#[tokio::main]
async fn main() -> Result<(), Box<dyn std::error::Error>> {
    std::env::set_var("RUST_LOG", &CFG.log.log_level);

    // init tracing
    tracing_subscriber::fmt()
        .with_max_level(tracing::Level::from_str(&CFG.log.log_level).unwrap())
        .with_env_filter(tracing_subscriber::EnvFilter::from_default_env())
        .init();

    // connect to database
    let db_conn = Database::connect(CFG.database.link.to_owned()).await?;

    let (tx, mut rx) = mpsc::unbounded_channel();

    for addr in &CFG.server.addr {
        let addr = SocketAddr::from_str(addr).unwrap();
        let tx = tx.clone();
        tracing::info!("Starting {} at {}", &CFG.server.name, addr);

        let health_svc = health_svc().await;

        let serve = Server::builder()
            .enable_ssl()?
            .add_grpc_service(db_conn.clone())
            .add_service(health_svc)
            .serve_with_shutdown(addr, shutdown_signal(addr));

        tokio::spawn(async move {
            if let Err(e) = serve.await {
                tracing::error!("server error: {}", e);
            }

            tx.send(()).unwrap();
        });
    }
    rx.recv().await;

    Ok(())
}

async fn shutdown_signal(addr: SocketAddr) {
    let ctrl_c = async {
        signal::ctrl_c()
            .await
            .expect("failed to install Ctrl+C handler");
    };

    #[cfg(unix)]
    let terminate = async {
        signal::unix::signal(signal::unix::SignalKind::terminate())
            .expect("failed to install signal handler")
            .recv()
            .await;
    };

    #[cfg(not(unix))]
    let terminate = std::future::pending::<()>();

    tokio::select! {
        _ = ctrl_c => {},
        _ = terminate => {},
    }

    println!("Shutting down server at {}", addr);
}
