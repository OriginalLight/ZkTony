
use axum::{
    routing::get,
    Router,
};

use super::system;

pub fn api() -> Router {
    Router::new()
    .route("/version", get(system::version::get_by_id))
}