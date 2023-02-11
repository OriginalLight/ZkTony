use axum::extract::Json;
use db::{common::res::Res, db_conn, system::models::log::LogAddReq, DB};
use service::system;

pub async fn add_batch(Json(req): Json<Vec<LogAddReq>>) -> Res<String> {
    let db = DB.get_or_init(db_conn).await;
    let res = system::log::add_batch(&db, req).await;

    match res {
        Ok(x) => Res::with_data(x),
        Err(e) => Res::with_err(&e.to_string()),
    }
}
