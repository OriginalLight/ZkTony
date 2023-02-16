use anyhow::Result;
use chrono::NaiveDateTime;
use db::system::{
    entities::customer::{self, Column},
    models::customer::{CustomerDeleteReq, CustomerGetReq, CustomerSaveReq},
    prelude::{CustomerEntity, CustomerModel},
};
use sea_orm::{
    sea_query::OnConflict, ColumnTrait, DatabaseConnection, EntityTrait, QueryFilter, Set,
};

// region: add
pub async fn add(db: &DatabaseConnection, req: CustomerSaveReq) -> Result<String> {
    let create_time =
        NaiveDateTime::parse_from_str(&req.create_time.unwrap(), "%Y-%m-%d %H:%M:%S").unwrap();
    let add_data = customer::ActiveModel {
        id: Set(req.id),
        name: Set(req.name),
        address: Set(req.address),
        phone: Set(req.phone),
        source: Set(req.source),
        industry: Set(req.industry),
        remarks: Set(req.remarks),
        create_by: Set(req.create_by),
        create_time: Set(Some(create_time)),
        ..Default::default()
    };
    let res = CustomerEntity::insert(add_data)
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
pub async fn update(db: &DatabaseConnection, req: CustomerSaveReq) -> Result<String> {
    let update_data = customer::ActiveModel {
        id: Set(req.id),
        name: Set(req.name),
        address: Set(req.address),
        phone: Set(req.phone),
        source: Set(req.source),
        industry: Set(req.industry),
        remarks: Set(req.remarks),
        ..Default::default()
    };
    let res = CustomerEntity::update(update_data)
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
pub async fn delete(db: &DatabaseConnection, req: CustomerDeleteReq) -> Result<String> {
    let res = CustomerEntity::delete_by_id(req.id)
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
pub async fn get(db: &DatabaseConnection, req: CustomerGetReq) -> Result<Vec<CustomerModel>> {
    let mut query = CustomerEntity::find();
    if let Some(x) = req.id {
        if !x.is_empty() {
            query = query.filter(customer::Column::Id.eq(x));
        }
    }

    if let Some(x) = req.name {
        if !x.is_empty() {
            query = query.filter(customer::Column::Name.like(&x));
        }
    }

    if let Some(x) = req.address {
        if !x.is_empty() {
            query = query.filter(customer::Column::Address.like(&x));
        }
    }

    if let Some(x) = req.phone {
        if !x.is_empty() {
            query = query.filter(customer::Column::Phone.like(&x));
        }
    }

    query
        .clone()
        .all(db)
        .await
        .map_err(|e| anyhow::anyhow!(e.to_string(),))
}
// endregion
