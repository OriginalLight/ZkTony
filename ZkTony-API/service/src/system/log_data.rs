use anyhow::Result;
use chrono::NaiveDateTime;
use db::system::{
    entities::log_data::{self, Column},
    models::log_data::LogDataAddReq,
    prelude::LogDataEntity,
};
use sea_orm::{sea_query::OnConflict, DatabaseConnection, EntityTrait, Set};

pub async fn add_batch(db: &DatabaseConnection, reqs: Vec<LogDataAddReq>) -> Result<String> {
    let mut add_data = Vec::new();
    for req in reqs {
        let create_time =
            NaiveDateTime::parse_from_str(&req.create_time.unwrap(), "%Y-%m-%d %H:%M:%S").unwrap();
        let add = log_data::ActiveModel {
            id: Set(req.id),
            log_id: Set(req.log_id),
            motor: Set(req.motor),
            voltage: Set(req.voltage),
            current: Set(req.current),
            time: Set(req.time),
            upload: Set(req.upload),
            create_time: Set(Some(create_time)),
            ..Default::default()
        };
        add_data.push(add);
    }

    let res = LogDataEntity::insert_many(add_data)
        .on_conflict(OnConflict::column(Column::Id).do_nothing().to_owned())
        .exec(db)
        .await
        .map_err(|e| anyhow::anyhow!(e.to_string(),));

    match res {
        Ok(_) => Ok("save log_data success".to_string()),
        Err(e) => Err(e),
    }
}
