use grpc_api::sea_orm::Database;
use std::env;
use tonic::transport::{Identity, Server, ServerTlsConfig};

use grpc_api::service;

#[tokio::main]
async fn main() -> Result<(), Box<dyn std::error::Error>> {
    dotenvy::dotenv().ok();

    tracing_subscriber::fmt()
        .with_max_level(tracing::Level::INFO)
        .init();

    let host = env::var("HOST").unwrap();
    let port = env::var("PORT").unwrap();
    let db_url = env::var("DATABASE_URL").unwrap();
    let cert_file_path = env::var("CERT_FILE_PATH").unwrap();
    let key_file_path = env::var("KEY_FILE_PATH").unwrap();

    let server_url = format!("{host}:{port}").parse().unwrap();
    let cert = std::fs::read_to_string(cert_file_path).unwrap();
    let key = std::fs::read_to_string(key_file_path).unwrap();

    let identity = Identity::from_pem(cert, key);
    let connection = Database::connect(&db_url).await?;
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
