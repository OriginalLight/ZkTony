use sea_orm::entity::prelude::*;
use serde::{Deserialize, Serialize};

#[derive(Debug, Clone, PartialEq, DeriveEntityModel, Serialize, Deserialize)]
#[sea_orm(table_name = "version")]
pub struct Model {
    #[sea_orm(primary_key)]
    #[serde(skip_deserializing)]
    pub id: i32,
    pub url: String,
    pub version_name: String,
    pub version_code: i32,
    pub description: String,
}

#[derive(Debug, Clone, Copy, EnumIter)]
pub enum Relation {
    Version,
}

impl RelationTrait for Relation {
    fn def(&self) -> sea_orm::RelationDef {
        match self {
            Self::Version => Entity::has_many(super::version::Entity).into(),
        }
    }
}
impl Related<super::version::Entity> for Entity {
    fn to() -> RelationDef {
        Relation::Version.def()
    }
}

impl ActiveModelBehavior for ActiveModel {}
