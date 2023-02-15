use serde::Deserialize;

#[derive(Deserialize, Debug, Clone)]
pub struct ShippingSaveReq {
    pub id: String,
    pub software_id: String,
    pub customer_id: String,
    pub product_id: String,
    pub product_number: String,
    pub producted_time: String,
    pub shipper: String,
    pub delivery_time: String,
    pub delivery_place: String,
    pub express_number: String,
    pub attachment: String,
    pub remarks: String,
    pub create_time: Option<String>,
}

#[derive(Deserialize, Debug, Clone)]
pub struct ShippingDeleteReq {
    pub id: String,
}

#[derive(Deserialize, Debug, Clone)]
pub struct ShippingGetReq {
    pub id: Option<String>,
    pub software_id: Option<String>,
    pub customer_id: Option<String>,
    pub product_id: Option<String>,
    pub product_number: Option<String>,
    pub shipper: Option<String>,
    pub express_number: Option<String>,
    pub begin_time: Option<String>,
    pub end_time: Option<String>,
}
