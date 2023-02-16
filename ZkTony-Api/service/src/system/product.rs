use anyhow::Result;
use chrono::NaiveDateTime;
use db::system::{
    entities::product::{self, Column},
    models::product::{ProductDeleteReq, ProductGetReq, ProductSaveReq},
    prelude::{ProductEntity, ProductModel},
};
use sea_orm::{
    sea_query::OnConflict, ColumnTrait, DatabaseConnection, EntityTrait, QueryFilter, Set,
};

// region: add
pub async fn add(db: &DatabaseConnection, req: ProductSaveReq) -> Result<String> {
    let create_time =
        NaiveDateTime::parse_from_str(&req.create_time.unwrap(), "%Y-%m-%d %H:%M:%S").unwrap();
    let equipment_time =
        NaiveDateTime::parse_from_str(&req.equipment_time, "%Y-%m-%d %H:%M:%S").unwrap();
    let add_data = product::ActiveModel {
        id: Set(req.id),
        software_id: Set(req.software_id),
        customer_id: Set(req.customer_id),
        equipment_id: Set(req.equipment_id),
        express_number: Set(req.express_number),
        express_company: Set(req.express_company),
        equipment_number: Set(req.equipment_number),
        equipment_time: Set(equipment_time),
        attachment: Set(req.attachment),
        remarks: Set(req.remarks),
        create_by: Set(req.create_by),
        create_time: Set(Some(create_time)),
        ..Default::default()
    };
    let res = ProductEntity::insert(add_data)
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
pub async fn update(db: &DatabaseConnection, req: ProductSaveReq) -> Result<String> {
    let equipment_time =
        NaiveDateTime::parse_from_str(&req.equipment_time, "%Y-%m-%d %H:%M:%S").unwrap();
    let update_data = product::ActiveModel {
        id: Set(req.id),
        software_id: Set(req.software_id),
        customer_id: Set(req.customer_id),
        equipment_id: Set(req.equipment_id),
        express_number: Set(req.express_number),
        express_company: Set(req.express_company),
        equipment_number: Set(req.equipment_number),
        equipment_time: Set(equipment_time),
        attachment: Set(req.attachment),
        remarks: Set(req.remarks),
        create_by: Set(req.create_by),
        ..Default::default()
    };
    let res = ProductEntity::update(update_data)
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
pub async fn delete(db: &DatabaseConnection, req: ProductDeleteReq) -> Result<String> {
    let res = ProductEntity::delete_by_id(req.id)
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
pub async fn get(db: &DatabaseConnection, req: ProductGetReq) -> Result<Vec<ProductModel>> {
    let mut query = ProductEntity::find();
    if let Some(x) = req.id {
        if !x.is_empty() {
            query = query.filter(product::Column::Id.eq(x));
        }
    }

    if let Some(x) = req.software_id {
        if !x.is_empty() {
            query = query.filter(product::Column::SoftwareId.eq(x));
        }
    }

    if let Some(x) = req.customer_id {
        if !x.is_empty() {
            query = query.filter(product::Column::CustomerId.eq(x));
        }
    }

    if let Some(x) = req.equipment_id {
        if !x.is_empty() {
            query = query.filter(product::Column::EquipmentId.eq(x));
        }
    }

    if let Some(x) = req.express_number {
        if !x.is_empty() {
            query = query.filter(product::Column::ExpressNumber.eq(x));
        }
    }

    if let Some(x) = req.express_company {
        if !x.is_empty() {
            query = query.filter(product::Column::ExpressCompany.like(&format!("%{}%", x)));
        }
    }

    if let Some(x) = req.equipment_number {
        if !x.is_empty() {
            query = query.filter(product::Column::EquipmentNumber.eq(x));
        }
    }

    if let Some(x) = req.create_by {
        if !x.is_empty() {
            query = query.filter(product::Column::CreateBy.eq(x));
        }
    }

    if let Some(x) = req.begin_time {
        if !x.is_empty() {
            let x = x + " 00:00:00";
            let t = NaiveDateTime::parse_from_str(&x, "%Y-%m-%d %H:%M:%S")?;
            query = query.filter(product::Column::CreateTime.gte(t));
        }
    }
    if let Some(x) = req.end_time {
        if !x.is_empty() {
            let x = x + " 23:59:59";
            let t = NaiveDateTime::parse_from_str(&x, "%Y-%m-%d %H:%M:%S")?;
            query = query.filter(product::Column::CreateTime.lte(t));
        }
    }

    query
        .clone()
        .all(db)
        .await
        .map_err(|e| anyhow::anyhow!(e.to_string(),))
}
// endregion
