use axum::extract::Json;
use database::{common::res::Res, db_conn, models::log::program::ProgramAddReq, DB};
use service::log::program;

pub async fn add_batch(Json(req): Json<Vec<ProgramAddReq>>) -> Res<String> {
    let db = DB.get_or_init(db_conn).await;
    let res = program::add_batch(&db, req).await;

    match res {
        Ok(x) => Res::with_data(x),
        Err(e) => Res::with_err(&e.to_string()),
    }
}
