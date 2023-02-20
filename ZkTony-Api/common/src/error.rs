use axum::{
    http::StatusCode,
    response::{IntoResponse, Response}, Json,
};
use serde_json::json;

#[derive(Debug)]
pub enum AppError {
    NotFound,
    DataBaseError,
    OtherError,
}

impl IntoResponse for AppError {
    fn into_response(self) -> Response {
        let (status, message) = match self {
            AppError::NotFound => (StatusCode::NOT_FOUND, "not found"),
            AppError::DataBaseError => (StatusCode::INTERNAL_SERVER_ERROR, "database error"),
            AppError::OtherError => (StatusCode::INTERNAL_SERVER_ERROR, "other error"),
        };
        let body = Json(json!(message));

        (status, body).into_response()
    }
}
