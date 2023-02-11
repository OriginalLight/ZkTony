use axum::{
    routing::{get, post},
    Router,
};

use super::system;

pub fn api() -> Router {
    Router::new()
        .nest("/application", application_api())
        .nest("/log", log_api())
        .nest("/program", program_api())
}

pub fn application_api() -> Router {
    Router::new().route("/", get(system::application::get_by_id))
}

pub fn log_api() -> Router {
    Router::new()
        .route("/", post(system::log::add_batch))
        .route("/detail", post(system::log_detail::add_batch))
}

pub fn program_api() -> Router {
    Router::new().route("/", post(system::program::add_batch))
}
