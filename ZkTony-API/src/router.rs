use axum::routing::get;

use crate::handler;

pub fn init() -> axum::Router {
    axum::Router::new().route("/version/:id", get(handler::version::get_by_id))
}
