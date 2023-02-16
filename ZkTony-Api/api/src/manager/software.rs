use axum::extract::{Json, Query};
use database::{
    common::res::Res,
    db_conn,
    entities::prelude::SoftwareModel,
    models::manager::software::{SoftwareDeleteReq, SoftwareGetReq, SoftwareSaveReq},
    DB,
};
use service::manager::software;

// region: get
pub async fn add(Json(req): Json<SoftwareSaveReq>) -> Res<String> {
    let db = DB.get_or_init(db_conn).await;
    let res = software::add(&db, req).await;

    match res {
        Ok(x) => Res::with_data(x),
        Err(e) => Res::with_err(&e.to_string()),
    }
}
// endregion

// region: update
pub async fn update(Json(req): Json<SoftwareSaveReq>) -> Res<String> {
    let db = DB.get_or_init(db_conn).await;
    let res = software::update(&db, req).await;

    match res {
        Ok(x) => Res::with_data(x),
        Err(e) => Res::with_err(&e.to_string()),
    }
}
// endregion

// region: delete
pub async fn delete(Json(req): Json<SoftwareDeleteReq>) -> Res<String> {
    let db = DB.get_or_init(db_conn).await;
    let res = software::delete(&db, req).await;

    match res {
        Ok(x) => Res::with_data(x),
        Err(e) => Res::with_err(&e.to_string()),
    }
}
// endregion

// region: get
pub async fn get(Query(req): Query<SoftwareGetReq>) -> Res<Vec<SoftwareModel>> {
    let db = DB.get_or_init(db_conn).await;
    let res = software::get(&db, req).await;

    match res {
        Ok(x) => Res::with_data(x),
        Err(e) => Res::with_err(&e.to_string()),
    }
}
// endregion
