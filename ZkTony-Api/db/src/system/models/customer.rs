use serde::Deserialize;

#[derive(Deserialize, Debug, Clone)]
pub struct CustomerSaveReq {
    pub id: String,
    pub name: String,
    pub phone: String,
    pub address: String,
    pub source: String,
    pub industry: String,
    pub remarks: Option<String>,
    pub create_by: Option<String>,
    pub create_time: Option<String>,
}

#[derive(Deserialize, Debug, Clone)]
pub struct CustomerDeleteReq {
    pub id: String,
}

#[derive(Deserialize, Debug, Clone)]
pub struct CustomerGetReq {
    pub id: Option<String>,
    pub name: Option<String>,
    pub phone: Option<String>,
    pub address: Option<String>,
}