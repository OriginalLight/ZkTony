use serde::Deserialize;

#[derive(Deserialize, Debug, Clone)]
pub struct ProductSaveReq {
    pub id: String,
    pub name: String,
    pub model: String,
    pub voltage: String,
    pub power: String,
    pub frequency: String,
    pub produced_address: String,
    pub produced_company: String,
    pub attachment: String,
    pub remarks: String,
    pub create_time: Option<String>,
}

#[derive(Deserialize, Debug, Clone)]
pub struct ProductDeleteReq {
    pub id: String,
}

#[derive(Deserialize, Debug, Clone)]
pub struct ProductGetReq {
    pub id: Option<String>,
    pub name: Option<String>,
    pub model: Option<String>,
}
