use sea_orm::entity::prelude::*;
use serde::{Deserialize, Serialize};

#[derive(Copy, Clone, Default, Debug, DeriveEntity)]
pub struct Entity;

impl EntityName for Entity {
    fn table_name(&self) -> &str {
        "shipping"
    }
}

#[derive(Clone, Debug, PartialEq, DeriveModel, DeriveActiveModel, Eq, Serialize, Deserialize)]
pub struct Model {
    pub id: String,
    pub software_id: String,
    pub customer_id: String,
    pub product_id: String,
    pub product_number: String,
    pub producted_time: DateTime,
    pub shipper: String,
    pub delivery_time: DateTime,
    pub delivery_place: String,
    pub express_number: String,
    pub attachment: String,
    pub remarks: String,
    pub create_time: Option<DateTime>,
}

#[derive(Copy, Clone, Debug, EnumIter, DeriveColumn)]
pub enum Column {
    Id,
    SoftwareId,
    CustomerId,
    ProductId,
    ProductNumber,
    ProductedTime,
    Shipper,
    DeliveryTime,
    DeliveryPlace,
    ExpressNumber,
    Attachment,
    Remarks,
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
            Self::ProductId => ColumnType::String(Some(32u32)).def(),
            Self::ProductNumber => ColumnType::String(Some(32u32)).def(),
            Self::ProductedTime => ColumnType::DateTime.def(),
            Self::Shipper => ColumnType::String(Some(32u32)).def(),
            Self::DeliveryTime => ColumnType::DateTime.def(),
            Self::DeliveryPlace => ColumnType::Text.def(),
            Self::ExpressNumber => ColumnType::Text.def(),
            Self::Attachment => ColumnType::Text.def(),
            Self::Remarks => ColumnType::Text.def(),
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
