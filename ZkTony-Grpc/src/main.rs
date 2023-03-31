use grpc_api::sea_orm::{ConnectOptions, Database};
use std::{env, time::Duration};
use tonic::transport::{Identity, Server, ServerTlsConfig};
use tracing::log;

use grpc_api::service;

#[tokio::main]
async fn main() -> Result<(), Box<dyn std::error::Error>> {
    dotenvy::dotenv().ok();

    tracing_subscriber::fmt()
        .with_max_level(tracing::Level::INFO)
        .pretty()
        .init();

    let host = env::var("HOST").unwrap();
    let port = env::var("PORT").unwrap();
    let db_url = env::var("DATABASE_URL").unwrap();
    let cert_file_path = env::var("CERT_FILE_PATH").unwrap();
    let key_file_path = env::var("KEY_FILE_PATH").unwrap();

    let server_url = format!("{host}:{port}").parse().unwrap();
    let cert = std::fs::read_to_string(cert_file_path).unwrap();
    let key = std::fs::read_to_string(key_file_path).unwrap();

    // create tls identity
    let identity = Identity::from_pem(cert, key);

    // connect to database
    let mut opt = ConnectOptions::new(db_url.to_owned());
    opt.max_connections(20)
        .min_connections(2)
        .connect_timeout(Duration::from_secs(8))
        .acquire_timeout(Duration::from_secs(8))
        .idle_timeout(Duration::from_secs(8))
        .max_lifetime(Duration::from_secs(8))
        .sqlx_logging(true)
        .sqlx_logging_level(log::LevelFilter::Info);

    let connection = Database::connect(opt).await?;

    // get service
    let health_svc = service::health_svc().await;
    let application_svc = service::application_svc(connection.clone());
    let log_svc = service::log_svc(connection.clone());
    let log_detail_svc = service::log_detail_svc(connection.clone());
    let program_svc = service::program_svc(connection.clone());

    tracing::info!("ZkTony-Grpc");
    tracing::info!("Starting server at {}", server_url);

    Server::builder()
        .tls_config(ServerTlsConfig::new().identity(identity))?
        .add_service(health_svc)
        .add_service(application_svc)
        .add_service(log_svc)
        .add_service(log_detail_svc)
        .add_service(program_svc)
        .serve(server_url)
        .await?;

    Ok(())
}
