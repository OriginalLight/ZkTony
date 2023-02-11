use axum::extract::Json;
use db::{common::res::Res, db_conn, system::models::log_data::LogDataAddReq, DB};
use service::system;

pub async fn add_batch(Json(req): Json<Vec<LogDataAddReq>>) -> Res<String> {
    let db = DB.get_or_init(db_conn).await;
    let res = system::log_data::add_batch(&db, req).await;

    match res {
        Ok(x) => Res::with_data(x),
        Err(e) => Res::with_err(&e.to_string()),
    }
}
