use serde::Deserialize;

#[derive(Deserialize, Debug, Clone)]
pub struct ProductSaveReq {
    pub id: String,
    pub software_id: String,
    pub customer_id: String,
    pub equipment_id: String,
    pub express_number: String,
    pub express_company: String,
    pub equipment_number: String,
    pub equipment_time: String,
    pub attachment: String,
    pub remarks: Option<String>,
    pub create_by: Option<String>,
    pub create_time: Option<String>,
}

#[derive(Deserialize, Debug, Clone)]
pub struct ProductDeleteReq {
    pub id: String,
}

#[derive(Deserialize, Debug, Clone)]
pub struct ProductGetReq {
    pub id: Option<String>,
    pub software_id: Option<String>,
    pub customer_id: Option<String>,
    pub equipment_id: Option<String>,
    pub express_number: Option<String>,
    pub express_company: Option<String>,
    pub equipment_number: Option<String>,
    pub create_by: Option<String>,
    pub begin_time: Option<String>,
    pub end_time: Option<String>,
}
