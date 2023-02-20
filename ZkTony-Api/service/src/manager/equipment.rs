use chrono::NaiveDateTime;
use common::error::AppError;
use database::{
    entities::{
        manager::equipment::{self, Column},
        prelude::{EquipmentEntity, EquipmentModel},
    },
    models::manager::equipment::{EquipmentDeleteReq, EquipmentGetReq, EquipmentSaveReq},
};
use sea_orm::{
    sea_query::OnConflict, ColumnTrait, DatabaseConnection, EntityTrait, QueryFilter, Set,
};

// region: add
pub async fn add(db: &DatabaseConnection, req: EquipmentSaveReq) -> Result<String, AppError> {
    let create_time =
        NaiveDateTime::parse_from_str(&req.create_time.unwrap(), "%Y-%m-%d %H:%M:%S").unwrap();
    let add_data = equipment::ActiveModel {
        id: Set(req.id),
        name: Set(req.name),
        model: Set(req.model),
        voltage: Set(req.voltage),
        power: Set(req.power),
        frequency: Set(req.frequency),
        attachment: Set(req.attachment),
        remarks: Set(req.remarks),
        create_by: Set(req.create_by),
        create_time: Set(Some(create_time)),
        ..Default::default()
    };
    let res = EquipmentEntity::insert(add_data)
        .on_conflict(OnConflict::column(Column::Id).do_nothing().to_owned())
        .exec(db)
        .await
        .map_err(|_| AppError::DataBaseError);

    match res {
        Ok(_) => Ok("Success".to_string()),
        Err(e) => Err(e),
    }
}
// endregion

// region: update
pub async fn update(db: &DatabaseConnection, req: EquipmentSaveReq) -> Result<String, AppError> {
    let update_data = equipment::ActiveModel {
        id: Set(req.id),
        name: Set(req.name),
        model: Set(req.model),
        voltage: Set(req.voltage),
        power: Set(req.power),
        frequency: Set(req.frequency),
        attachment: Set(req.attachment),
        remarks: Set(req.remarks),
        create_by: Set(req.create_by),
        ..Default::default()
    };
    let res = EquipmentEntity::update(update_data)
        .exec(db)
        .await
        .map_err(|_| AppError::DataBaseError);

    match res {
        Ok(_) => Ok("Success".to_string()),
        Err(e) => Err(e),
    }
}
// endregion

// region: delete
pub async fn delete(db: &DatabaseConnection, req: EquipmentDeleteReq) -> Result<String, AppError> {
    let res = EquipmentEntity::delete_by_id(req.id)
        .exec(db)
        .await
        .map_err(|_| AppError::DataBaseError);

    match res {
        Ok(_) => Ok("Success".to_string()),
        Err(e) => Err(e),
    }
}
// endregion

// region: get
pub async fn get(db: &DatabaseConnection, req: EquipmentGetReq) -> Result<Option<Vec<EquipmentModel>>, AppError> {
    let mut query = EquipmentEntity::find();
    if let Some(x) = req.id {
        if !x.is_empty() {
            query = query.filter(equipment::Column::Id.eq(x));
        }
    }

    if let Some(x) = req.name {
        if !x.is_empty() {
            query = query.filter(equipment::Column::Name.like(&format!("%{}%", x)));
        }
    }

    if let Some(x) = req.model {
        if !x.is_empty() {
            query = query.filter(equipment::Column::Model.like(&format!("%{}%", x)));
        }
    }

    if let Some(x) = req.begin_time {
        if !x.is_empty() {
            let x = x + " 00:00:00";
            let t = NaiveDateTime::parse_from_str(&x, "%Y-%m-%d %H:%M:%S").unwrap();
            query = query.filter(equipment::Column::CreateTime.gte(t));
        }
    }
    if let Some(x) = req.end_time {
        if !x.is_empty() {
            let x = x + " 23:59:59";
            let t = NaiveDateTime::parse_from_str(&x, "%Y-%m-%d %H:%M:%S").unwrap();
            query = query.filter(equipment::Column::CreateTime.lte(t));
        }
    }

    let res = query
        .clone()
        .all(db)
        .await
        .map_err(|_| AppError::DataBaseError);

    match res {
        Ok(x) => {
            if x.is_empty() {
                Ok(None)
            } else {
                Ok(Some(x))
            }
        }
        Err(e) => Err(e),
    }
}
// endregion
