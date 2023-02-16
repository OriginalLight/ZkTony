use anyhow::Result;
use chrono::NaiveDateTime;
use db::system::{
    entities::software::{self, Column},
    models::software::{SoftwareDeleteReq, SoftwareGetReq, SoftwareSaveReq},
    prelude::{SoftwareEntity, SoftwareModel},
};
use sea_orm::{
    sea_query::OnConflict, ColumnTrait, DatabaseConnection, EntityTrait, QueryFilter, Set,
};

// region: add
pub async fn add(db: &DatabaseConnection, req: SoftwareSaveReq) -> Result<String> {
    let create_time =
        NaiveDateTime::parse_from_str(&req.create_time.unwrap(), "%Y-%m-%d %H:%M:%S").unwrap();
    let add_data = software::ActiveModel {
        id: Set(req.id),
        package: Set(req.package),
        version_code: Set(req.version_code),
        version_name: Set(req.version_name),
        build_type: Set(req.build_type),
        remarks: Set(req.remarks),
        create_time: Set(Some(create_time)),
        ..Default::default()
    };
    let res = SoftwareEntity::insert(add_data)
        .on_conflict(OnConflict::column(Column::Id).do_nothing().to_owned())
        .exec(db)
        .await
        .map_err(|e| anyhow::anyhow!(e.to_string(),));

    match res {
        Ok(_) => Ok("Add software success".to_string()),
        Err(e) => Err(e),
    }
}
// endregion

// region: update
pub async fn update(db: &DatabaseConnection, req: SoftwareSaveReq) -> Result<String> {
    let update_data = software::ActiveModel {
        id: Set(req.id),
        package: Set(req.package),
        version_code: Set(req.version_code),
        version_name: Set(req.version_name),
        build_type: Set(req.build_type),
        remarks: Set(req.remarks),
        ..Default::default()
    };
    let res = SoftwareEntity::update(update_data)
        .exec(db)
        .await
        .map_err(|e| anyhow::anyhow!(e.to_string(),));

    match res {
        Ok(_) => Ok("Update software success".to_string()),
        Err(e) => Err(e),
    }
}
// endregion

// region: delete
pub async fn delete(db: &DatabaseConnection, req: SoftwareDeleteReq) -> Result<String> {
    let res = SoftwareEntity::delete_by_id(req.id)
        .exec(db)
        .await
        .map_err(|e| anyhow::anyhow!(e.to_string(),));

    match res {
        Ok(_) => Ok("Delete software success".to_string()),
        Err(e) => Err(e),
    }
}
// endregion

// region: get
pub async fn get(db: &DatabaseConnection, req: SoftwareGetReq) -> Result<Vec<SoftwareModel>> {
    let mut query = SoftwareEntity::find();
    if let Some(x) = req.id {
        if !x.is_empty() {
            query = query.filter(software::Column::Id.eq(x));
        }
    }

    if let Some(x) = req.package {
        if !x.is_empty() {
            query = query.filter(software::Column::Package.eq(x));
        }
    }

    if let Some(x) = req.build_type {
        if !x.is_empty() {
            query = query.filter(software::Column::BuildType.eq(x));
        }
    }

    query
        .clone()
        .all(db)
        .await
        .map_err(|e| anyhow::anyhow!(e.to_string(),))
}
// endregion
