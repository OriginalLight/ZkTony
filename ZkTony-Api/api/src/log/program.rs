use axum::extract::Json;
use common::error::AppError;
use database::{db_conn, models::log::program::ProgramAddReq, DB};
use service::log::program;

pub async fn add_batch(Json(req): Json<Vec<ProgramAddReq>>) -> Result<Json<String>, AppError> {
    let db = DB.get_or_init(db_conn).await;
    let res = program::add_batch(&db, req).await;

    match res {
        Ok(x) => Ok(Json(x)),
        Err(e) => Err(e),
    }

}
