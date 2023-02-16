use axum::extract::{Json, Query};
use database::{
    common::res::Res,
    db_conn,
    entities::prelude::ProductModel,
    models::manager::product::{ProductDeleteReq, ProductGetReq, ProductSaveReq},
    DB,
};
use service::manager::product;

// region: add
pub async fn add(Json(req): Json<ProductSaveReq>) -> Res<String> {
    let db = DB.get_or_init(db_conn).await;
    let res = product::add(&db, req).await;

    match res {
        Ok(x) => Res::with_data(x),
        Err(e) => Res::with_err(&e.to_string()),
    }
}
// endregion

// region: update
pub async fn update(Json(req): Json<ProductSaveReq>) -> Res<String> {
    let db = DB.get_or_init(db_conn).await;
    let res = product::update(&db, req).await;

    match res {
        Ok(x) => Res::with_data(x),
        Err(e) => Res::with_err(&e.to_string()),
    }
}
// endregion

// region: delete
pub async fn delete(Json(req): Json<ProductDeleteReq>) -> Res<String> {
    let db = DB.get_or_init(db_conn).await;
    let res = product::delete(&db, req).await;

    match res {
        Ok(x) => Res::with_data(x),
        Err(e) => Res::with_err(&e.to_string()),
    }
}
// endregion

// region: get
pub async fn get(Query(req): Query<ProductGetReq>) -> Res<Vec<ProductModel>> {
    let db = DB.get_or_init(db_conn).await;
    let res = product::get(&db, req).await;

    match res {
        Ok(x) => Res::with_data(x),
        Err(e) => Res::with_err(&e.to_string()),
    }
}
// endregion
