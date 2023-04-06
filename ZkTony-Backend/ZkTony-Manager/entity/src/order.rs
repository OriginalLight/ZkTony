use sea_orm::entity::prelude::*;
use serde::{Deserialize, Serialize};

#[derive(Copy, Clone, Default, Debug, DeriveEntity)]
pub struct Entity;

impl EntityName for Entity {
    fn table_name(&self) -> &str {
        "order"
    }
}

#[derive(Clone, Debug, PartialEq, DeriveModel, DeriveActiveModel, Eq, Serialize, Deserialize)]
pub struct Model {
    pub id: String,
    pub software_id: String,
    pub customer_id: String,
    pub instrument_id: String,
    pub express_number: String,
    pub express_company: String,
    pub instrument_number: String,
    pub instrument_time: Option<DateTime>,
    pub attachment: String,
    pub remarks: Option<String>,
    pub create_by: Option<String>,
    pub create_time: Option<DateTime>,
}

#[derive(Copy, Clone, Debug, EnumIter, DeriveColumn)]
pub enum Column {
    Id,
    SoftwareId,
    CustomerId,
    InstrumentId,
    ExpressNumber,
    ExpressCompany,
    InstrumentNumber,
    InstrumentTime,
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
            Self::SoftwareId => ColumnType::String(Some(32u32)).def(),
            Self::CustomerId => ColumnType::String(Some(32u32)).def(),
            Self::InstrumentId => ColumnType::String(Some(32u32)).def(),
            Self::ExpressNumber => ColumnType::String(Some(32u32)).def(),
            Self::ExpressCompany => ColumnType::String(Some(32u32)).def(),
            Self::InstrumentNumber => ColumnType::String(Some(32u32)).def(),
            Self::InstrumentTime => ColumnType::DateTime.def(),
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
