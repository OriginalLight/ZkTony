use axum::extract::{Json, Query};
use db::{
    common::res::Res,
    db_conn,
    system::{
        models::software::{SoftwareDeleteReq, SoftwareGetReq, SoftwareSaveReq},
        prelude::SoftwareModel,
    },
    DB,
};
use service::system;

pub async fn add(Json(req): Json<SoftwareSaveReq>) -> Res<String> {
    let db = DB.get_or_init(db_conn).await;
    let res = system::software::add(&db, req).await;

    match res {
        Ok(x) => Res::with_data(x),
        Err(e) => Res::with_err(&e.to_string()),
    }
}

pub async fn update(Json(req): Json<SoftwareSaveReq>) -> Res<String> {
    let db = DB.get_or_init(db_conn).await;
    let res = system::software::update(&db, req).await;

    match res {
        Ok(x) => Res::with_data(x),
        Err(e) => Res::with_err(&e.to_string()),
    }
}

pub async fn delete(Json(req): Json<SoftwareDeleteReq>) -> Res<String> {
    let db = DB.get_or_init(db_conn).await;
    let res = system::software::delete(&db, req).await;

    match res {
        Ok(x) => Res::with_data(x),
        Err(e) => Res::with_err(&e.to_string()),
    }
}

pub async fn get(Query(req): Query<SoftwareGetReq>) -> Res<Vec<SoftwareModel>> {
    let db = DB.get_or_init(db_conn).await;
    let res = system::software::get(&db, req).await;

    match res {
        Ok(x) => Res::with_data(x),
        Err(e) => Res::with_err(&e.to_string()),
    }
}
