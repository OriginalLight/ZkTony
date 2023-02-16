use anyhow::Result;
use chrono::NaiveDateTime;
use database::{
    entities::{
        log::log_detail::{self, Column},
        prelude::LogDetailEntity,
    },
    models::log::log_detail::LogDetailAddReq,
};
use sea_orm::{sea_query::OnConflict, DatabaseConnection, EntityTrait, Set};

// region: add_batch
pub async fn add_batch(db: &DatabaseConnection, reqs: Vec<LogDetailAddReq>) -> Result<String> {
    let mut add_data = Vec::new();
    for req in reqs {
        let create_time =
            NaiveDateTime::parse_from_str(&req.create_time.unwrap(), "%Y-%m-%d %H:%M:%S").unwrap();
        let add = log_detail::ActiveModel {
            id: Set(req.id),
            log_id: Set(req.log_id),
            content: Set(req.content),
            create_time: Set(Some(create_time)),
            ..Default::default()
        };
        add_data.push(add);
    }

    let res = LogDetailEntity::insert_many(add_data)
        .on_conflict(OnConflict::column(Column::Id).do_nothing().to_owned())
        .exec(db)
        .await
        .map_err(|e| anyhow::anyhow!(e.to_string(),));

    match res {
        Ok(_) => Ok("save log_data success".to_string()),
        Err(e) => Err(e),
    }
}
// endregion
