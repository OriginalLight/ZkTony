use sea_orm::entity::prelude::*;
use serde::{Deserialize, Serialize};

#[derive(Copy, Clone, Default, Debug, DeriveEntity)]
pub struct Entity;

impl EntityName for Entity {
    fn table_name(&self) -> &str {
        "software"
    }
}

#[derive(Clone, Debug, PartialEq, DeriveModel, DeriveActiveModel, Eq, Serialize, Deserialize)]
pub struct Model {
    pub id: String,
    pub package: String,
    pub version_code: i32,
    pub version_name: String,
    pub build_type: String,
    pub remarks: Option<String>,
    pub create_by: Option<String>,
    pub create_time: Option<DateTime>,
}

#[derive(Copy, Clone, Debug, EnumIter, DeriveColumn)]
pub enum Column {
    Id,
    Package,
    VersionName,
    VersionCode,
    BuildType,
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
            Self::Package => ColumnType::String(Some(32u32)).def(),
            Self::VersionCode => ColumnType::Integer.def(),
            Self::VersionName => ColumnType::String(Some(32u32)).def(),
            Self::BuildType => ColumnType::String(Some(32u32)).def(),
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
