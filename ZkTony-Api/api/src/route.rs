use axum::{
    routing::{get, post},
    Router,
};

use super::system::{
    application, customer, index, log, log_detail, product, program, shipping, software,
};

pub fn api() -> Router {
    Router::new()
        .nest("/", index_api())
        .nest("/application", application_api())
        .nest("/customer", customer_api())
        .nest("/log", log_api())
        .nest("/product", product_api())
        .nest("/program", program_api())
        .nest("/shipping", shipping_api())
        .nest("/software", software_api())
}

pub fn index_api() -> Router {
    Router::new().route("/", get(index::index))
}

pub fn application_api() -> Router {
    Router::new().route("/", get(application::get_by_id))
}

pub fn customer_api() -> Router {
    Router::new().route(
        "/",
        get(customer::get)
            .post(customer::add)
            .put(customer::update)
            .delete(customer::delete),
    )
}

pub fn product_api() -> Router {
    Router::new().route(
        "/",
        get(product::get)
            .post(product::add)
            .put(product::update)
            .delete(product::delete),
    )
}

pub fn program_api() -> Router {
    Router::new().route("/", post(program::add_batch))
}

pub fn log_api() -> Router {
    Router::new()
        .route("/", post(log::add_batch))
        .route("/detail", post(log_detail::add_batch))
}

pub fn shipping_api() -> Router {
    Router::new().route(
        "/",
        get(shipping::get)
            .post(shipping::add)
            .put(shipping::update)
            .delete(shipping::delete),
    )
}

pub fn software_api() -> Router {
    Router::new().route(
        "/",
        get(software::get)
            .post(software::add)
            .put(software::update)
            .delete(software::delete),
    )
}
