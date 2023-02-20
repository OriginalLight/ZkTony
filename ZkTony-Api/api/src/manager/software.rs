use axum::extract::{Json, Query};
use common::error::AppError;
use database::{
    db_conn,
    entities::prelude::SoftwareModel,
    models::manager::software::{SoftwareDeleteReq, SoftwareGetReq, SoftwareSaveReq},
    DB,
};
use service::manager::software;

// region: get
pub async fn add(Json(req): Json<SoftwareSaveReq>) -> Result<Json<String>, AppError> {
    let db = DB.get_or_init(db_conn).await;
    let res = software::add(&db, req).await;

    match res {
        Ok(x) => Ok(Json(x)),
        Err(e) => Err(e),
    }
}
// endregion

// region: update
pub async fn update(Json(req): Json<SoftwareSaveReq>) -> Result<Json<String>, AppError> {
    let db = DB.get_or_init(db_conn).await;
    let res = software::update(&db, req).await;

    match res {
        Ok(x) => Ok(Json(x)),
        Err(e) => Err(e),
    }
}
// endregion

// region: delete
pub async fn delete(Json(req): Json<SoftwareDeleteReq>) -> Result<Json<String>, AppError> {
    let db = DB.get_or_init(db_conn).await;
    let res = software::delete(&db, req).await;
    
    match res {
        Ok(x) => Ok(Json(x)),
        Err(e) => Err(e),
    }
}
// endregion

// region: get
pub async fn get(
    Query(req): Query<SoftwareGetReq>,
) -> Result<Json<Option<Vec<SoftwareModel>>>, AppError> {
    let db = DB.get_or_init(db_conn).await;
    let res = software::get(&db, req).await;

    match res {
        Ok(x) => Ok(Json(x)),
        Err(e) => Err(e),
    }
}
// endregion
