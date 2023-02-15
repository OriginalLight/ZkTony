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

pub async fn add(db: &DatabaseConnection, req: ProductSaveReq) -> Result<String> {
    let create_time =
        NaiveDateTime::parse_from_str(&req.create_time.unwrap(), "%Y-%m-%d %H:%M:%S").unwrap();
    let add_data = product::ActiveModel {
        id: Set(req.id),
        name: Set(req.name),
        model: Set(req.model),
        voltage: Set(req.voltage),
        power: Set(req.power),
        frequency: Set(req.frequency),
        produced_address: Set(req.produced_address),
        produced_company: Set(req.produced_company),
        attachment: Set(req.attachment),
        remarks: Set(req.remarks),
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

pub async fn update(db: &DatabaseConnection, req: ProductSaveReq) -> Result<String> {
    let update_data = product::ActiveModel {
        id: Set(req.id),
        name: Set(req.name),
        model: Set(req.model),
        voltage: Set(req.voltage),
        power: Set(req.power),
        frequency: Set(req.frequency),
        produced_address: Set(req.produced_address),
        produced_company: Set(req.produced_company),
        attachment: Set(req.attachment),
        remarks: Set(req.remarks),
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

pub async fn get(db: &DatabaseConnection, req: ProductGetReq) -> Result<Vec<ProductModel>> {
    let mut query = ProductEntity::find();
    if let Some(x) = req.id {
        if !x.is_empty() {
            query = query.filter(product::Column::Id.eq(x));
        }
    }

    if let Some(x) = req.name {
        if !x.is_empty() {
            query = query.filter(product::Column::Name.like(&x));
        }
    }

    if let Some(x) = req.model {
        if !x.is_empty() {
            query = query.filter(product::Column::Model.like(&x));
        }
    }

    query
        .clone()
        .all(db)
        .await
        .map_err(|e| anyhow::anyhow!(e.to_string(),))
}
