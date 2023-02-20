use axum::extract::Json;
use common::error::AppError;
use database::{db_conn, models::log::log_detail::LogDetailAddReq, DB};
use service::log::log_detail;

// region: add_batch
pub async fn add_batch(Json(req): Json<Vec<LogDetailAddReq>>) -> Result<Json<String>, AppError> {
    let db = DB.get_or_init(db_conn).await;
    let res = log_detail::add_batch(&db, req).await;

    match res {
        Ok(x) => Ok(Json(x)),
        Err(e) => Err(e),
    }

}
// endregion
