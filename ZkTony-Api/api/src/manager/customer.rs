use axum::extract::{Json, Query};
use common::error::AppError;
use database::{
    db_conn,
    entities::prelude::CustomerModel,
    models::manager::customer::{CustomerDeleteReq, CustomerGetReq, CustomerSaveReq},
    DB,
};
use service::manager::customer;

// region: add
pub async fn add(Json(req): Json<CustomerSaveReq>) -> Result<Json<String>, AppError> {
    let db = DB.get_or_init(db_conn).await;
    let res = customer::add(&db, req).await;

    match res {
        Ok(x) => Ok(Json(x)),
        Err(e) => Err(e),
    }
}
// endregion

// region: update
pub async fn update(Json(req): Json<CustomerSaveReq>) -> Result<Json<String>, AppError> {
    let db = DB.get_or_init(db_conn).await;
    let res = customer::update(&db, req).await;

    match res {
        Ok(x) => Ok(Json(x)),
        Err(e) => Err(e),
    }
}
// endregion

// region: delete
pub async fn delete(Json(req): Json<CustomerDeleteReq>) -> Result<Json<String>, AppError> {
    let db = DB.get_or_init(db_conn).await;
    let res = customer::delete(&db, req).await;

    match res {
        Ok(x) => Ok(Json(x)),
        Err(e) => Err(e),
    }
}
// endregion

// region: get
pub async fn get(Query(req): Query<CustomerGetReq>) -> Result<Json<Option<Vec<CustomerModel>>>, AppError> {
    let db = DB.get_or_init(db_conn).await;
    let res = customer::get(&db, req).await;


    match res {
        Ok(v) => Ok(Json(v)),
        Err(e) => Err(e),
    }
}
// endregion
