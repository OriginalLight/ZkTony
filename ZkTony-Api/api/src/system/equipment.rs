use axum::extract::Json;
use db::{
    common::res::Res,
    db_conn,
    system::{
        models::equipment::{EquipmentDeleteReq, EquipmentGetReq, EquipmentSaveReq},
        prelude::EquipmentModel,
    },
    DB,
};
use service::system;

pub async fn add(Json(req): Json<EquipmentSaveReq>) -> Res<String> {
    let db = DB.get_or_init(db_conn).await;
    let res = system::equipment::add(&db, req).await;

    match res {
        Ok(x) => Res::with_data(x),
        Err(e) => Res::with_err(&e.to_string()),
    }
}

pub async fn update(Json(req): Json<EquipmentSaveReq>) -> Res<String> {
    let db = DB.get_or_init(db_conn).await;
    let res = system::equipment::update(&db, req).await;

    match res {
        Ok(x) => Res::with_data(x),
        Err(e) => Res::with_err(&e.to_string()),
    }
}

pub async fn delete(Json(req): Json<EquipmentDeleteReq>) -> Res<String> {
    let db = DB.get_or_init(db_conn).await;
    let res = system::equipment::delete(&db, req).await;

    match res {
        Ok(x) => Res::with_data(x),
        Err(e) => Res::with_err(&e.to_string()),
    }
}

pub async fn get(Json(req): Json<EquipmentGetReq>) -> Res<Vec<EquipmentModel>> {
    let db = DB.get_or_init(db_conn).await;
    let res = system::equipment::get(&db, req).await;

    match res {
        Ok(x) => Res::with_data(x),
        Err(e) => Res::with_err(&e.to_string()),
    }
}
