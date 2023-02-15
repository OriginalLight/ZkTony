use axum::extract::Json;
use db::{
    common::res::Res,
    db_conn,
    system::{
        models::customer::{CustomerDeleteReq, CustomerGetReq, CustomerSaveReq},
        prelude::CustomerModel,
    },
    DB,
};
use service::system;

pub async fn add(Json(req): Json<CustomerSaveReq>) -> Res<String> {
    let db = DB.get_or_init(db_conn).await;
    let res = system::customer::add(&db, req).await;

    match res {
        Ok(x) => Res::with_data(x),
        Err(e) => Res::with_err(&e.to_string()),
    }
}

pub async fn update(Json(req): Json<CustomerSaveReq>) -> Res<String> {
    let db = DB.get_or_init(db_conn).await;
    let res = system::customer::update(&db, req).await;

    match res {
        Ok(x) => Res::with_data(x),
        Err(e) => Res::with_err(&e.to_string()),
    }
}

pub async fn delete(Json(req): Json<CustomerDeleteReq>) -> Res<String> {
    let db = DB.get_or_init(db_conn).await;
    let res = system::customer::delete(&db, req).await;

    match res {
        Ok(x) => Res::with_data(x),
        Err(e) => Res::with_err(&e.to_string()),
    }
}

pub async fn get(Json(req): Json<CustomerGetReq>) -> Res<Vec<CustomerModel>> {
    let db = DB.get_or_init(db_conn).await;
    let res = system::customer::get(&db, req).await;

    match res {
        Ok(x) => Res::with_data(x),
        Err(e) => Res::with_err(&e.to_string()),
    }
}
