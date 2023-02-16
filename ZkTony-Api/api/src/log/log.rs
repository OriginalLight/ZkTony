use axum::extract::Json;
use database::{common::res::Res, db_conn, models::log::log::LogAddReq, DB};
use service::log::log;

// region: add_batch
pub async fn add_batch(Json(req): Json<Vec<LogAddReq>>) -> Res<String> {
    let db = DB.get_or_init(db_conn).await;
    let res = log::add_batch(&db, req).await;

    match res {
        Ok(x) => Res::with_data(x),
        Err(e) => Res::with_err(&e.to_string()),
    }
}
// endregion
