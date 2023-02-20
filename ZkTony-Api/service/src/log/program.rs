use chrono::NaiveDateTime;
use common::error::AppError;
use database::{
    entities::{
        log::program::{self, Column},
        prelude::ProgramEntity,
    },
    models::log::program::ProgramAddReq,
};
use sea_orm::{sea_query::OnConflict, DatabaseConnection, EntityTrait, Set};

// region: add_batch
pub async fn add_batch(db: &DatabaseConnection, reqs: Vec<ProgramAddReq>) -> Result<String, AppError> {
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
        .map_err(|_| AppError::DataBaseError);

    match res {
        Ok(_) => Ok("Success".to_string()),
        Err(e) => Err(e),
    }
}
// endregion
