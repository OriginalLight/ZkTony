use axum::extract::Json;
use db::{
    common::res::Res,
    db_conn,
    system::{
        models::shipping::{ShippingDeleteReq, ShippingGetReq, ShippingSaveReq},
        prelude::ShippingModel,
    },
    DB,
};
use service::system;

pub async fn add(Json(req): Json<ShippingSaveReq>) -> Res<String> {
    let db = DB.get_or_init(db_conn).await;
    let res = system::shipping::add(&db, req).await;

    match res {
        Ok(x) => Res::with_data(x),
        Err(e) => Res::with_err(&e.to_string()),
    }
}

pub async fn update(Json(req): Json<ShippingSaveReq>) -> Res<String> {
    let db = DB.get_or_init(db_conn).await;
    let res = system::shipping::update(&db, req).await;

    match res {
        Ok(x) => Res::with_data(x),
        Err(e) => Res::with_err(&e.to_string()),
    }
}

pub async fn delete(Json(req): Json<ShippingDeleteReq>) -> Res<String> {
    let db = DB.get_or_init(db_conn).await;
    let res = system::shipping::delete(&db, req).await;

    match res {
        Ok(x) => Res::with_data(x),
        Err(e) => Res::with_err(&e.to_string()),
    }
}

pub async fn get(Json(req): Json<ShippingGetReq>) -> Res<Vec<ShippingModel>> {
    let db = DB.get_or_init(db_conn).await;
    let res = system::shipping::get(&db, req).await;

    match res {
        Ok(x) => Res::with_data(x),
        Err(e) => Res::with_err(&e.to_string()),
    }
}
