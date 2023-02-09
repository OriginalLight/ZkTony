use sea_orm::entity::prelude::*;
use serde::{Deserialize, Serialize};

#[derive(Debug, Clone, PartialEq, DeriveEntityModel, Serialize, Deserialize)]
#[sea_orm(table_name = "categoies")]
pub struct Model {
    #[sea_orm(primary_key)]
    #[serde(skip_deserializing)]
    pub id: i32,
    pub name: String,
    pub is_del: bool,
}

#[derive(Debug, Clone, Copy, EnumIter)]
pub enum Relation {
    Articles,
}

impl RelationTrait for Relation {
    fn def(&self) -> sea_orm::RelationDef {
        match self {
            Self::Articles => Entity::has_many(super::article::Entity).into(),
        }
    }
}
impl Related<super::article::Entity> for Entity {
    fn to() -> RelationDef {
        Relation::Articles.def()
    }
}

impl ActiveModelBehavior for ActiveModel {}
