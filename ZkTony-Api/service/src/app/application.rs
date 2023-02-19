use axum::http::StatusCode;
use database::entities::{
    app::application,
    prelude::{ApplicationEntity, ApplicationModel},
};
use sea_orm::{ColumnTrait, DatabaseConnection, EntityTrait, QueryFilter};

// region: gey_by_id
pub async fn get_by_id(
    db: &DatabaseConnection,
    application_id: String,
) -> Result<ApplicationModel, (StatusCode, String)> {
    let v = ApplicationEntity::find()
        .filter(application::Column::ApplicationId.eq(application_id))
        .one(db)
        .await
        .map_err(|e| (StatusCode::INTERNAL_SERVER_ERROR, e.to_string()));

    match v {
        Ok(m) => match m {
            Some(v) => Ok(v),
            None => Err((StatusCode::NOT_FOUND, "Not Found".to_string())),
        },
        Err(e) => Err(e),
    }
}
// endregion
