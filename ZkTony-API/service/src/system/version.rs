use anyhow::{anyhow, Result};
use db::system::entities::version;
use sea_orm::{
    DatabaseConnection, EntityTrait
};

pub async fn get_by_id(db: &DatabaseConnection, id: i32) ->Result<version::Model> {

    let v = version::Entity::find_by_id(id).one(db).await?;
    let res = match v {
        Some(m) => m,
        None => return Err(anyhow!("没有找到数据",)),
    };
    Ok(res)
}
 