use anyhow::{anyhow, Result};
use database::entities::{
    app::application,
    prelude::{ApplicationEntity, ApplicationModel},
};
use sea_orm::{ColumnTrait, DatabaseConnection, EntityTrait, QueryFilter};

// region: gey_by_id
pub async fn get_by_id(
    db: &DatabaseConnection,
    application_id: String,
) -> Result<ApplicationModel> {
    let v = ApplicationEntity::find()
        .filter(application::Column::ApplicationId.eq(application_id))
        .one(db)
        .await?;

    let res = match v {
        Some(m) => m,
        None => return Err(anyhow!("没有找到数据",)),
    };
    Ok(res)
}
// endregion
