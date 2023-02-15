use serde::Deserialize;

#[derive(Deserialize, Debug, Clone)]
pub struct ProductAddReq {
    pub id: String,
    pub name: String,
    pub model: String,
    pub voltage: String,
    pub power: String,
    pub frequency: String,
    pub produced_time: String,
    pub produced_address: String,
    pub produced_company: String,
    pub attachment: String,
    pub remarks: String,
    pub create_time: Option<String>,
}

#[derive(Deserialize, Debug, Clone)]
pub struct ProductUpdateReq {
    pub id: String,
    pub name: String,
    pub model: String,
    pub voltage: String,
    pub power: String,
    pub frequency: String,
    pub produced_time: String,
    pub produced_address: String,
    pub produced_company: String,
    pub attachment: String,
    pub remarks: String,
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
    pub voltage: Option<String>,
    pub power: Option<String>,
    pub frequency: Option<String>,
    pub produced_time: Option<String>,
    pub produced_address: Option<String>,
    pub produced_company: Option<String>,
}

