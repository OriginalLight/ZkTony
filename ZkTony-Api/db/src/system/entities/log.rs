use sea_orm::entity::prelude::*;
use serde::{Deserialize, Serialize};

#[derive(Copy, Clone, Default, Debug, DeriveEntity)]
pub struct Entity;

impl EntityName for Entity {
    fn table_name(&self) -> &str {
        "log"
    }
}

#[derive(Clone, Debug, PartialEq, DeriveModel, DeriveActiveModel, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct Model {
    pub id: String,
    pub sub_id: String,
    pub log_type: String,
    pub content: String,
    pub create_time: Option<DateTime>,
}

#[derive(Copy, Clone, Debug, EnumIter, DeriveColumn)]
pub enum Column {
    Id,
    SubId,
    LogType,
    Content,
    CreateTime,
}

#[derive(Copy, Clone, Debug, EnumIter, DerivePrimaryKey)]
pub enum PrimaryKey {
    Id,
}

impl PrimaryKeyTrait for PrimaryKey {
    type ValueType = String;
    fn auto_increment() -> bool {
        false
    }
}

#[derive(Copy, Clone, Debug, EnumIter)]
pub enum Relation {}

impl ColumnTrait for Column {
    type EntityName = Entity;
    fn def(&self) -> ColumnDef {
        match self {
            Self::Id => ColumnType::String(Some(32u32)).def(),
            Self::SubId => ColumnType::String(Some(32u32)).def(),
            Self::LogType => ColumnType::String(Some(32u32)).def(),
            Self::Content => ColumnType::Text.def(),
            Self::CreateTime => ColumnType::DateTime.def().null(),
        }
    }
}

impl RelationTrait for Relation {
    fn def(&self) -> RelationDef {
        panic!("No RelationDef")
    }
}

impl ActiveModelBehavior for ActiveModel {}
