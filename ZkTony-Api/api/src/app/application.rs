use axum::extract::Query;
use database::{
    common::res::Res, db_conn, entities::prelude::ApplicationModel,
    models::app::application::ApplicationSearchReq, DB,
};
use service::app::application;
// region: get_by_id
pub async fn get_by_id(Query(req): Query<ApplicationSearchReq>) -> Res<ApplicationModel> {
    let db = DB.get_or_init(db_conn).await;
    let res = application::get_by_id(db, req.application_id).await;

    match res {
        Ok(x) => Res::with_data(x),
        Err(e) => Res::with_err(&e.to_string()),
    }
}
// endregion
