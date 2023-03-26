use actix_web::{middleware, web, App, HttpServer};
use api::{handler::not_found, router, sea_orm::Database, state::AppState};

use std::env;

#[actix_web::main]
async fn main() -> std::io::Result<()> {
    std::env::set_var("RUST_LOG", "debug");
    // init logger
    tracing_subscriber::fmt()
        .with_env_filter(tracing_subscriber::EnvFilter::from_default_env())
        .with_writer(std::io::stdout)
        .init();

    // get env vars
    dotenvy::dotenv().ok();
    let db_url = env::var("DATABASE_URL").expect("DATABASE_URL is not set in .env file");
    let host = env::var("HOST").expect("HOST is not set in .env file");
    let port = env::var("PORT").expect("PORT is not set in .env file");
    let server_url = format!("{host}:{port}");

    let conn = Database::connect(&db_url).await.unwrap();

    // build app state
    let state = AppState { conn };

    // log
    show_log();
    tracing::info!("Starting server at {}", server_url);

    HttpServer::new(move || {
        App::new()
            .app_data(web::Data::new(state.clone()))
            .wrap(middleware::Logger::default()) // enable logger
            .default_service(web::route().to(not_found))
            .configure(router::init)
    })
    .bind(&server_url)?
    .run()
    .await
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
    tracing::info!("{}", logo);
    tracing::info!("编译器：{}", std::env::consts::DLL_EXTENSION);
    tracing::info!("系统架构：{}", std::env::consts::OS);
    tracing::info!("系统类型：{}", std::env::consts::ARCH);
    tracing::info!("操作系统：{}", std::env::consts::FAMILY);
}
