use grpc_api::sea_orm::{ConnectOptions, Database};
use std::{net::SocketAddr, str::FromStr, time::Duration};
use tonic::{
    codec::CompressionEncoding,
    transport::{Identity, Server, ServerTlsConfig},
};
use tracing::log;

use grpc_api::{service, CFG};

#[tokio::main]
async fn main() -> Result<(), Box<dyn std::error::Error>> {
    std::env::set_var("RUST_LOG", &CFG.log.log_level);

    tracing_subscriber::fmt()
        .with_max_level(tracing::Level::from_str(&CFG.log.log_level).unwrap())
        .with_env_filter(tracing_subscriber::EnvFilter::from_default_env())
        .init();

    let mut server = Server::builder();

    if CFG.server.ssl {
        tracing::info!("SSL is enabled");

        let cert = std::fs::read_to_string(&CFG.cert.cert).unwrap();
        let key = std::fs::read_to_string(&CFG.cert.key).unwrap();

        // create tls identity
        let identity = Identity::from_pem(cert, key);
        server = server.tls_config(ServerTlsConfig::new().identity(identity))?;
    }

    // connect to database
    let mut opt = ConnectOptions::new(CFG.database.link.to_owned());

    opt.max_connections(20)
        .min_connections(2)
        .connect_timeout(Duration::from_secs(8))
        .acquire_timeout(Duration::from_secs(8))
        .idle_timeout(Duration::from_secs(8))
        .max_lifetime(Duration::from_secs(8))
        .sqlx_logging(true)
        .sqlx_logging_level(log::LevelFilter::from_str(&CFG.log.log_level).unwrap());

    let connection = Database::connect(opt).await?;

    // get service
    let mut health_svc = service::health_svc().await;
    let mut application_svc = service::application_svc(connection.clone());
    let mut log_svc = service::log_svc(connection.clone());
    let mut log_detail_svc = service::log_detail_svc(connection.clone());
    let mut program_svc = service::program_svc(connection.clone());

    if CFG.server.content_gzip {
        tracing::info!("Content gzip is enabled");
        health_svc = health_svc
            .send_compressed(CompressionEncoding::Gzip)
            .send_compressed(CompressionEncoding::Gzip);
        application_svc = application_svc
            .send_compressed(CompressionEncoding::Gzip)
            .send_compressed(CompressionEncoding::Gzip);
        log_svc = log_svc
            .send_compressed(CompressionEncoding::Gzip)
            .send_compressed(CompressionEncoding::Gzip);
        log_detail_svc = log_detail_svc
            .send_compressed(CompressionEncoding::Gzip)
            .send_compressed(CompressionEncoding::Gzip);
        program_svc = program_svc
            .send_compressed(CompressionEncoding::Gzip)
            .send_compressed(CompressionEncoding::Gzip);
    }

    let addr = SocketAddr::from_str(&CFG.server.address).unwrap();

    tracing::info!("{} Starting server at {}", &CFG.server.name, addr);

    server
        .add_service(health_svc)
        .add_service(application_svc)
        .add_service(log_svc)
        .add_service(log_detail_svc)
        .add_service(program_svc)
        .serve(addr)
        .await?;

    Ok(())
}
