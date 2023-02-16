use axum::extract::{Json, Query};
use database::{
    common::res::Res,
    db_conn,
    entities::prelude::CustomerModel,
    models::manager::customer::{CustomerDeleteReq, CustomerGetReq, CustomerSaveReq},
    DB,
};
use service::manager::customer;

// region: add
pub async fn add(Json(req): Json<CustomerSaveReq>) -> Res<String> {
    let db = DB.get_or_init(db_conn).await;
    let res = customer::add(&db, req).await;

    match res {
        Ok(x) => Res::with_data(x),
        Err(e) => Res::with_err(&e.to_string()),
    }
}
// endregion

// region: update
pub async fn update(Json(req): Json<CustomerSaveReq>) -> Res<String> {
    let db = DB.get_or_init(db_conn).await;
    let res = customer::update(&db, req).await;

    match res {
        Ok(x) => Res::with_data(x),
        Err(e) => Res::with_err(&e.to_string()),
    }
}
// endregion

// region: delete
pub async fn delete(Json(req): Json<CustomerDeleteReq>) -> Res<String> {
    let db = DB.get_or_init(db_conn).await;
    let res = customer::delete(&db, req).await;

    match res {
        Ok(x) => Res::with_data(x),
        Err(e) => Res::with_err(&e.to_string()),
    }
}
// endregion

// region: get
pub async fn get(Query(req): Query<CustomerGetReq>) -> Res<Vec<CustomerModel>> {
    let db = DB.get_or_init(db_conn).await;
    let res = customer::get(&db, req).await;

    match res {
        Ok(x) => Res::with_data(x),
        Err(e) => Res::with_err(&e.to_string()),
    }
}
// endregion
