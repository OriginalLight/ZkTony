use std::future::ready;

use axum::{routing::get, Router};

use super::system;

pub fn api() -> Router {
    let recorder_handle = system::metrics::setup_metrics_recorder();
    Router::new().route("/", get(move || ready(recorder_handle.render())))
}
