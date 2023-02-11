use anyhow::Result;
use chrono::NaiveDateTime;
use db::system::{
    entities::log::{self, Column},
    models::log::LogAddReq,
    prelude::LogEntity,
};
use sea_orm::{sea_query::OnConflict, DatabaseConnection, EntityTrait, Set};

pub async fn add_batch(db: &DatabaseConnection, reqs: Vec<LogAddReq>) -> Result<String> {
    let mut add_data = Vec::new();
    for req in reqs {
        let create_time =
            NaiveDateTime::parse_from_str(&req.create_time.unwrap(), "%Y-%m-%d %H:%M:%S").unwrap();
        let add = log::ActiveModel {
            id: Set(req.id),
            program_id: Set(req.program_id),
            motor: Set(req.motor),
            voltage: Set(req.voltage),
            time: Set(req.time),
            model: Set(req.model),
            upload: Set(req.upload),
            create_time: Set(Some(create_time)),
            ..Default::default()
        };
        add_data.push(add);
    }
    let res = LogEntity::insert_many(add_data)
        .on_conflict(OnConflict::column(Column::Id).do_nothing().to_owned())
        .exec(db)
        .await
        .map_err(|e| anyhow::anyhow!(e.to_string(),));

    match res {
        Ok(_) => Ok("save log success".to_string()),
        Err(e) => Err(e),
    }
}
