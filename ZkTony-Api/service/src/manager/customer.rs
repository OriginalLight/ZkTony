use chrono::NaiveDateTime;
use common::error::AppError;
use database::{
    entities::{
        manager::customer::{self, Column},
        prelude::{CustomerEntity, CustomerModel},
    },
    models::manager::customer::{CustomerDeleteReq, CustomerGetReq, CustomerSaveReq},
};
use sea_orm::{
    sea_query::OnConflict, ColumnTrait, DatabaseConnection, EntityTrait, QueryFilter, Set,
};

// region: add
pub async fn add(db: &DatabaseConnection, req: CustomerSaveReq) -> Result<String, AppError> {
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
        .map_err(|_| AppError::DataBaseError);

    match res {
        Ok(_) => Ok("Success".to_string()),
        Err(e) => Err(e),
    }
}
// endregion

// region: update
pub async fn update(db: &DatabaseConnection, req: CustomerSaveReq) -> Result<String, AppError> {
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
        .map_err(|_| AppError::DataBaseError);

    match res {
        Ok(_) => Ok("Success".to_string()),
        Err(e) => Err(e),
    }
}
// endregion

// region: delete
pub async fn delete(db: &DatabaseConnection, req: CustomerDeleteReq) -> Result<String, AppError> {
    let res = CustomerEntity::delete_by_id(req.id)
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
pub async fn get(
    db: &DatabaseConnection,
    req: CustomerGetReq,
) -> Result<Option<Vec<CustomerModel>>, AppError> {
    let mut query = CustomerEntity::find();
    if let Some(x) = req.id {
        if !x.is_empty() {
            query = query.filter(customer::Column::Id.eq(x));
        }
    }

    if let Some(x) = req.name {
        if !x.is_empty() {
            query = query.filter(customer::Column::Name.like(&format!("%{}%", x)));
        }
    }

    if let Some(x) = req.address {
        if !x.is_empty() {
            query = query.filter(customer::Column::Address.like(&format!("%{}%", x)));
        }
    }

    if let Some(x) = req.phone {
        if !x.is_empty() {
    
            query = query.filter(customer::Column::Phone.like(&format!("%{}%", x)));
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
