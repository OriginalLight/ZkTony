use sea_orm::entity::prelude::*;
use serde::{Deserialize, Serialize};

#[derive(Copy, Clone, Default, Debug, DeriveEntity)]
pub struct Entity;

impl EntityName for Entity {
    fn table_name(&self) -> &str {
        "instrument"
    }
}

#[derive(Clone, Debug, PartialEq, DeriveModel, DeriveActiveModel, Eq, Serialize, Deserialize)]
pub struct Model {
    pub id: String,
    pub name: String,
    pub model: String,
    pub voltage: String,
    pub power: String,
    pub frequency: String,
    pub attachment: String,
    pub remarks: Option<String>,
    pub create_by: Option<String>,
    pub create_time: Option<DateTime>,
}

#[derive(Copy, Clone, Debug, EnumIter, DeriveColumn)]
pub enum Column {
    Id,
    Name,
    Model,
    Voltage,
    Power,
    Frequency,
    Attachment,
    Remarks,
    CreateBy,
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
            Self::Name => ColumnType::String(Some(32u32)).def(),
            Self::Model => ColumnType::String(Some(32u32)).def(),
            Self::Voltage => ColumnType::String(Some(32u32)).def(),
            Self::Power => ColumnType::String(Some(32u32)).def(),
            Self::Frequency => ColumnType::String(Some(32u32)).def(),
            Self::Attachment => ColumnType::Text.def(),
            Self::Remarks => ColumnType::Text.def(),
            Self::CreateBy => ColumnType::String(Some(32u32)).def(),
            Self::CreateTime => ColumnType::DateTime.def(),
        }
    }
}

impl RelationTrait for Relation {
    fn def(&self) -> RelationDef {
        panic!("No RelationDef")
    }
}

impl ActiveModelBehavior for ActiveModel {}
