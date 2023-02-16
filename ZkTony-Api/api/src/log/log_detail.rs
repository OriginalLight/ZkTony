use axum::extract::Json;
use database::{common::res::Res, db_conn, models::log::log_detail::LogDetailAddReq, DB};
use service::log::log_detail;

// region: add_batch
pub async fn add_batch(Json(req): Json<Vec<LogDetailAddReq>>) -> Res<String> {
    let db = DB.get_or_init(db_conn).await;
    let res = log_detail::add_batch(&db, req).await;

    match res {
        Ok(x) => Res::with_data(x),
        Err(e) => Res::with_err(&e.to_string()),
    }
}
// endregion
