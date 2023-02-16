use axum::extract::{Json, Query};
use db::{
    common::res::Res,
    db_conn,
    system::{
        models::product::{ProductDeleteReq, ProductGetReq, ProductSaveReq},
        prelude::ProductModel,
    },
    DB,
};
use service::system;

pub async fn add(Json(req): Json<ProductSaveReq>) -> Res<String> {
    let db = DB.get_or_init(db_conn).await;
    let res = system::product::add(&db, req).await;

    match res {
        Ok(x) => Res::with_data(x),
        Err(e) => Res::with_err(&e.to_string()),
    }
}

pub async fn update(Json(req): Json<ProductSaveReq>) -> Res<String> {
    let db = DB.get_or_init(db_conn).await;
    let res = system::product::update(&db, req).await;

    match res {
        Ok(x) => Res::with_data(x),
        Err(e) => Res::with_err(&e.to_string()),
    }
}

pub async fn delete(Json(req): Json<ProductDeleteReq>) -> Res<String> {
    let db = DB.get_or_init(db_conn).await;
    let res = system::product::delete(&db, req).await;

    match res {
        Ok(x) => Res::with_data(x),
        Err(e) => Res::with_err(&e.to_string()),
    }
}

pub async fn get(Query(req): Query<ProductGetReq>) -> Res<Vec<ProductModel>> {
    let db = DB.get_or_init(db_conn).await;
    let res = system::product::get(&db, req).await;

    match res {
        Ok(x) => Res::with_data(x),
        Err(e) => Res::with_err(&e.to_string()),
    }
}
