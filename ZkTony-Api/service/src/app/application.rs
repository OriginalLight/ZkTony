use common::error::AppError;
use database::entities::{
    app::application,
    prelude::{ApplicationEntity, ApplicationModel},
};
use sea_orm::{ColumnTrait, DatabaseConnection, EntityTrait, QueryFilter};

// region: gey_by_id
pub async fn get_by_id(
    db: &DatabaseConnection,
    application_id: String,
) -> Result<Option<ApplicationModel>, AppError> {
    ApplicationEntity::find()
        .filter(application::Column::ApplicationId.eq(application_id))
        .one(db)
        .await
        .map_err(|_| AppError::DataBaseError)
}
// endregion
