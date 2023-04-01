use grpc_api::{health::health_svc, sea_orm::Database, service::ServerExt, CFG};
use std::{net::SocketAddr, str::FromStr};
use tonic::transport::{Identity, Server, ServerTlsConfig};

#[tokio::main]
async fn main() -> Result<(), Box<dyn std::error::Error>> {
    std::env::set_var("RUST_LOG", &CFG.log.log_level);

    // init tracing
    tracing_subscriber::fmt()
        .with_max_level(tracing::Level::from_str(&CFG.log.log_level).unwrap())
        .with_env_filter(tracing_subscriber::EnvFilter::from_default_env())
        .init();

    tracing::info!("Starting server...");

    let mut server = Server::builder();

    // init ssl
    if CFG.server.ssl {
        tracing::info!("SSL is enabled");

        let cert = std::fs::read_to_string(&CFG.cert.cert).unwrap();
        let key = std::fs::read_to_string(&CFG.cert.key).unwrap();

        // create tls identity
        let identity = Identity::from_pem(cert, key);
        server = server.tls_config(ServerTlsConfig::new().identity(identity))?;
    }

    // connect to database
    let db_conn = Database::connect(CFG.database.link.to_owned()).await?;

    // start server
    let addr = SocketAddr::from_str(&CFG.server.address).unwrap();

    tracing::info!("Starting {} at {}", &CFG.server.name, addr);

    let health_svc = health_svc().await;

    server
        .add_grpc_service(db_conn)
        .add_service(health_svc)
        .serve(addr)
        .await?;

    Ok(())
}
