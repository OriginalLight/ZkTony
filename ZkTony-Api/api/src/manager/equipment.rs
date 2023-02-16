use axum::extract::{Json, Query};
use database::{
    common::res::Res,
    db_conn,
    entities::prelude::EquipmentModel,
    models::manager::equipment::{EquipmentDeleteReq, EquipmentGetReq, EquipmentSaveReq},
    DB,
};
use service::manager::equipment;

// region: add
pub async fn add(Json(req): Json<EquipmentSaveReq>) -> Res<String> {
    let db = DB.get_or_init(db_conn).await;
    let res = equipment::add(&db, req).await;

    match res {
        Ok(x) => Res::with_data(x),
        Err(e) => Res::with_err(&e.to_string()),
    }
}
// endregion

// region: update
pub async fn update(Json(req): Json<EquipmentSaveReq>) -> Res<String> {
    let db = DB.get_or_init(db_conn).await;
    let res = equipment::update(&db, req).await;

    match res {
        Ok(x) => Res::with_data(x),
        Err(e) => Res::with_err(&e.to_string()),
    }
}
// endregion

// region: delete
pub async fn delete(Json(req): Json<EquipmentDeleteReq>) -> Res<String> {
    let db = DB.get_or_init(db_conn).await;
    let res = equipment::delete(&db, req).await;

    match res {
        Ok(x) => Res::with_data(x),
        Err(e) => Res::with_err(&e.to_string()),
    }
}
// endregion

// region: get
pub async fn get(Query(req): Query<EquipmentGetReq>) -> Res<Vec<EquipmentModel>> {
    let db = DB.get_or_init(db_conn).await;
    let res = equipment::get(&db, req).await;

    match res {
        Ok(x) => Res::with_data(x),
        Err(e) => Res::with_err(&e.to_string()),
    }
}
// endregion
