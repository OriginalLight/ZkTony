use anyhow::Result;
use chrono::NaiveDateTime;
use db::system::{
    entities::program::{self, Column},
    models::program::ProgramAddReq,
    prelude::ProgramEntity,
};
use sea_orm::{sea_query::OnConflict, DatabaseConnection, EntityTrait, Set};

pub async fn add_batch(db: &DatabaseConnection, reqs: Vec<ProgramAddReq>) -> Result<String> {
    let mut add_data = Vec::new();
    for req in reqs {
        let create_time =
            NaiveDateTime::parse_from_str(&req.create_time.unwrap(), "%Y-%m-%d %H:%M:%S").unwrap();
        let add = program::ActiveModel {
            id: Set(req.id),
            name: Set(req.name),
            content: Set(req.content),
            create_time: Set(Some(create_time)),
            ..Default::default()
        };
        add_data.push(add);
    }
    let res = ProgramEntity::insert_many(add_data)
        .on_conflict(OnConflict::column(Column::Id).do_nothing().to_owned())
        .exec(db)
        .await
        .map_err(|e| anyhow::anyhow!(e.to_string(),));

    match res {
        Ok(_) => Ok("save log success".to_string()),
        Err(e) => Err(e),
    }
}
