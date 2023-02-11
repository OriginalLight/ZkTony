use axum::extract::Query;
use db::{
    common::res::Res,
    db_conn,
    system::{models::version::VersionDbSearchReq, prelude::VersionModel},
    DB,
};
use service::system;

pub async fn get_by_id(Query(req): Query<VersionDbSearchReq>) -> Res<VersionModel> {
    let db = DB.get_or_init(db_conn).await;
    let res = system::version::get_by_id(db, req.id).await;

    match res {
        Ok(x) => Res::with_data(x),
        Err(e) => Res::with_err(&e.to_string()),
    }
}
