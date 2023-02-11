use axum::extract::Json;
use db::{common::res::Res, db_conn, system::models::program::ProgramAddReq, DB};
use service::system;

pub async fn add_batch(Json(req): Json<Vec<ProgramAddReq>>) -> Res<String> {
    let db = DB.get_or_init(db_conn).await;
    let res = system::program::add_batch(&db, req).await;

    match res {
        Ok(x) => Res::with_data(x),
        Err(e) => Res::with_err(&e.to_string()),
    }
}
