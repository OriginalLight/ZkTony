use axum::{extract::Query, Json};
use common::error::AppError;
use database::{
    db_conn, entities::prelude::ApplicationModel, models::app::application::ApplicationSearchReq,
    DB,
};
use service::app::application;
// region: get_by_id
pub async fn get_by_id(
    Query(req): Query<ApplicationSearchReq>,
) -> Result<Json<Option<ApplicationModel>>, AppError> {
    let db = DB.get_or_init(db_conn).await;
    let res = application::get_by_id(db, req.application_id).await;

    match res {
        Ok(v) => Ok(Json(v)),
        Err(e) => Err(e),
    }
}
// endregion
