use axum::extract::{Json, Query};
use common::error::AppError;
use database::{
    db_conn,
    entities::prelude::EquipmentModel,
    models::manager::equipment::{EquipmentDeleteReq, EquipmentGetReq, EquipmentSaveReq},
    DB,
};
use service::manager::equipment;

// region: add
pub async fn add(Json(req): Json<EquipmentSaveReq>) -> Result<Json<String>, AppError> {
    let db = DB.get_or_init(db_conn).await;
    let  res = equipment::add(&db, req).await;
    
    match res {
        Ok(x) => Ok(Json(x)),
        Err(e) => Err(e),
    }
}
// endregion

// region: update
pub async fn update(Json(req): Json<EquipmentSaveReq>) -> Result<Json<String>, AppError> {
    let db = DB.get_or_init(db_conn).await;
    let res = equipment::update(&db, req).await;

    match res {
        Ok(x) => Ok(Json(x)),
        Err(e) => Err(e),
    }
}
// endregion

// region: delete
pub async fn delete(Json(req): Json<EquipmentDeleteReq>) -> Result<Json<String>, AppError> {
    let db = DB.get_or_init(db_conn).await;
    let res = equipment::delete(&db, req).await;

    match res {
        Ok(x) => Ok(Json(x)),
        Err(e) => Err(e),
    }
}
// endregion

// region: get
pub async fn get(
    Query(req): Query<EquipmentGetReq>,
) -> Result<Json<Option<Vec<EquipmentModel>>>, AppError> {
    let db = DB.get_or_init(db_conn).await;
    let res = equipment::get(&db, req).await;

    match res {
        Ok(x) => Ok(Json(x)),
        Err(e) => Err(e),
    }
}
// endregion
