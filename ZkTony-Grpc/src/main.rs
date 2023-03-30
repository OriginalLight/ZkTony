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

    let db_url = env::var("DATABASE_URL").expect("DATABASE_URL is not set in .env file");
    let host = env::var("HOST").expect("HOST is not set in .env file");
    let port = env::var("PORT").expect("PORT is not set in .env file");
    let server_url = format!("{host}:{port}").parse()?;
    let cert = std::fs::read_to_string("tls/server.pem")?;
    let key = std::fs::read_to_string("tls/server.key")?;

    let identity = Identity::from_pem(cert, key);

    // establish database connection
    let connection = Database::connect(&db_url).await?;

    let health_svc = service::health_svc().await;
    let application_svc = service::application_svc(connection.clone());
    let log_svc = service::log_svc(connection.clone());
    let log_detail_svc = service::log_detail_svc(connection.clone());
    let program_svc = service::program_svc(connection.clone());

    tracing::info!("ZkTony-Grpc-Server");
    tracing::info!("Starting server at {}", server_url);
    tracing::info!("编译器：{}", std::env::consts::DLL_EXTENSION);
    tracing::info!("系统架构：{}", std::env::consts::OS);
    tracing::info!("系统类型：{}", std::env::consts::ARCH);
    tracing::info!("操作系统：{}", std::env::consts::FAMILY);

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
