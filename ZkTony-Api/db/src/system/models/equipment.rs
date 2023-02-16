use serde::Deserialize;

#[derive(Deserialize, Debug, Clone)]
pub struct EquipmentSaveReq {
    pub id: String,
    pub name: String,
    pub model: String,
    pub voltage: String,
    pub power: String,
    pub frequency: String,
    pub attachment: String,
    pub remarks: Option<String>,
    pub create_by: Option<String>,
    pub create_time: Option<String>,
}

#[derive(Deserialize, Debug, Clone)]
pub struct EquipmentDeleteReq {
    pub id: String,
}

#[derive(Deserialize, Debug, Clone)]
pub struct EquipmentGetReq {
    pub id: Option<String>,
    pub name: Option<String>,
    pub model: Option<String>,
    pub begin_time: Option<String>,
    pub end_time: Option<String>,
}
