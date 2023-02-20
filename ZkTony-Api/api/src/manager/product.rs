use axum::extract::{Json, Query};
use common::error::AppError;
use database::{
    db_conn,
    entities::prelude::ProductModel,
    models::manager::product::{ProductDeleteReq, ProductGetReq, ProductSaveReq},
    DB,
};
use service::manager::product;

// region: add
pub async fn add(Json(req): Json<ProductSaveReq>) -> Result<Json<String>, AppError> {
    let db = DB.get_or_init(db_conn).await;
    let res = product::add(&db, req).await;

    match res {
        Ok(x) => Ok(Json(x)),
        Err(e) => Err(e),
    }
}
// endregion

// region: update
pub async fn update(Json(req): Json<ProductSaveReq>) -> Result<Json<String>, AppError> {
    let db = DB.get_or_init(db_conn).await;
    let res = product::update(&db, req).await;

    match res {
        Ok(x) => Ok(Json(x)),
        Err(e) => Err(e),
    }
}
// endregion

// region: delete
pub async fn delete(Json(req): Json<ProductDeleteReq>) -> Result<Json<String>, AppError> {
    let db = DB.get_or_init(db_conn).await;
    let res = product::delete(&db, req).await;

    match res {
        Ok(x) => Ok(Json(x)),
        Err(e) => Err(e),
    }
}
// endregion

// region: get
pub async fn get(
    Query(req): Query<ProductGetReq>,
) -> Result<Json<Option<Vec<ProductModel>>>, AppError> {
    let db = DB.get_or_init(db_conn).await;
    let res = product::get(&db, req).await;

    match res {
        Ok(x) => Ok(Json(x)),
        Err(e) => Err(e),
    }
}
// endregion
