use anyhow::Result;
use chrono::NaiveDateTime;
use db::system::{
    entities::shipping::{self, Column},
    models::shipping::{ShippingDeleteReq, ShippingGetReq, ShippingSaveReq},
    prelude::{ShippingEntity, ShippingModel},
};
use sea_orm::{
    sea_query::OnConflict, ColumnTrait, DatabaseConnection, EntityTrait, QueryFilter, Set,
};

pub async fn add(db: &DatabaseConnection, req: ShippingSaveReq) -> Result<String> {
    let create_time =
        NaiveDateTime::parse_from_str(&req.create_time.unwrap(), "%Y-%m-%d %H:%M:%S").unwrap();
    let producted_time =
        NaiveDateTime::parse_from_str(&req.producted_time, "%Y-%m-%d %H:%M:%S").unwrap();
    let delivery_time =
        NaiveDateTime::parse_from_str(&req.delivery_time, "%Y-%m-%d %H:%M:%S").unwrap();
    let add_data = shipping::ActiveModel {
        id: Set(req.id),
        software_id: Set(req.software_id),
        customer_id: Set(req.customer_id),
        product_id: Set(req.product_id),
        product_number: Set(req.product_number),
        producted_time: Set(producted_time),
        shipper: Set(req.shipper),
        delivery_time: Set(delivery_time),
        delivery_place: Set(req.delivery_place),
        express_number: Set(req.express_number),
        attachment: Set(req.attachment),
        remarks: Set(req.remarks),
        create_time: Set(Some(create_time)),
        ..Default::default()
    };
    let res = ShippingEntity::insert(add_data)
        .on_conflict(OnConflict::column(Column::Id).do_nothing().to_owned())
        .exec(db)
        .await
        .map_err(|e| anyhow::anyhow!(e.to_string(),));

    match res {
        Ok(_) => Ok("Add software success".to_string()),
        Err(e) => Err(e),
    }
}

pub async fn update(db: &DatabaseConnection, req: ShippingSaveReq) -> Result<String> {
    let producted_time =
        NaiveDateTime::parse_from_str(&req.producted_time, "%Y-%m-%d %H:%M:%S").unwrap();
    let delivery_time =
        NaiveDateTime::parse_from_str(&req.delivery_time, "%Y-%m-%d %H:%M:%S").unwrap();
    let update_data = shipping::ActiveModel {
        id: Set(req.id),
        software_id: Set(req.software_id),
        customer_id: Set(req.customer_id),
        product_id: Set(req.product_id),
        product_number: Set(req.product_number),
        producted_time: Set(producted_time),
        shipper: Set(req.shipper),
        delivery_time: Set(delivery_time),
        delivery_place: Set(req.delivery_place),
        express_number: Set(req.express_number),
        attachment: Set(req.attachment),
        remarks: Set(req.remarks),
        ..Default::default()
    };
    let res = ShippingEntity::update(update_data)
        .exec(db)
        .await
        .map_err(|e| anyhow::anyhow!(e.to_string(),));

    match res {
        Ok(_) => Ok("Update software success".to_string()),
        Err(e) => Err(e),
    }
}

pub async fn delete(db: &DatabaseConnection, req: ShippingDeleteReq) -> Result<String> {
    let res = ShippingEntity::delete_by_id(req.id)
        .exec(db)
        .await
        .map_err(|e| anyhow::anyhow!(e.to_string(),));

    match res {
        Ok(_) => Ok("Delete software success".to_string()),
        Err(e) => Err(e),
    }
}

pub async fn get(db: &DatabaseConnection, req: ShippingGetReq) -> Result<Vec<ShippingModel>> {
    let mut query = ShippingEntity::find();
    if let Some(x) = req.id {
        if !x.is_empty() {
            query = query.filter(shipping::Column::Id.eq(x));
        }
    }

    if let Some(x) = req.software_id {
        if !x.is_empty() {
            query = query.filter(shipping::Column::SoftwareId.eq(x));
        }
    }

    if let Some(x) = req.customer_id {
        if !x.is_empty() {
            query = query.filter(shipping::Column::CustomerId.eq(x));
        }
    }

    if let Some(x) = req.product_id {
        if !x.is_empty() {
            query = query.filter(shipping::Column::ProductId.eq(x));
        }
    }

    if let Some(x) = req.product_number {
        if !x.is_empty() {
            query = query.filter(shipping::Column::ProductNumber.eq(x));
        }
    }

    if let Some(x) = req.shipper {
        if !x.is_empty() {
            query = query.filter(shipping::Column::Shipper.eq(x));
        }
    }

    if let Some(x) = req.begin_time {
        if !x.is_empty() {
            let x = x + " 00:00:00";
            let t = NaiveDateTime::parse_from_str(&x, "%Y-%m-%d %H:%M:%S")?;
            query = query.filter(shipping::Column::DeliveryTime.gte(t));
        }
    }
    if let Some(x) = req.end_time {
        if !x.is_empty() {
            let x = x + " 23:59:59";
            let t = NaiveDateTime::parse_from_str(&x, "%Y-%m-%d %H:%M:%S")?;
            query = query.filter(shipping::Column::DeliveryTime.lte(t));
        }
    }

    if let Some(x) = req.express_number {
        if !x.is_empty() {
            query = query.filter(shipping::Column::ExpressNumber.eq(x));
        }
    }

    query
        .clone()
        .all(db)
        .await
        .map_err(|e| anyhow::anyhow!(e.to_string(),))
}
