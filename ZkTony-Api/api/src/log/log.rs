use axum::extract::Json;
use common::error::AppError;
use database::{db_conn, models::log::log::LogAddReq, DB};
use service::log::log;

// region: add_batch
pub async fn add_batch(Json(req): Json<Vec<LogAddReq>>) -> Result<Json<String>, AppError> {
    let db = DB.get_or_init(db_conn).await;
    let res = log::add_batch(&db, req).await;

    match res {
        Ok(x) => Ok(Json(x)),
        Err(e) => Err(e),
    }
}
// endregion
