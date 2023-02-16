use std::future::ready;

use super::handler;
use axum::{routing::get, Router};

pub fn api() -> Router {
    Router::new().route(
        "/",
        get(move || ready(handler::setup_metrics_recorder().render())),
    )
}
