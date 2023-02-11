use axum::{
    routing::{get, post},
    Router,
};

use super::system;

pub fn api() -> Router {
    Router::new()
        .nest("/version", version_api())
        .nest("/log", log_api())
}

pub fn version_api() -> Router {
    Router::new().route("/", get(system::version::get_by_id))
}

pub fn log_api() -> Router {
    Router::new()
        .route("/", post(system::log::add_batch))
        .route("/data", post(system::log_data::add_batch))
}
