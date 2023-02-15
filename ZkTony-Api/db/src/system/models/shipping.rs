use serde::Deserialize;

#[derive(Deserialize, Debug, Clone)]
pub struct ShippingAddReq {
    pub id: String,
    pub software_id: String,
    pub customer_id: String,
    pub product_id: String,
    pub shipper: String,
    pub delivery_time: String,
    pub delivery_place: String,
    pub express_number: String,
    pub attachment: String,
    pub remarks: String,
    pub create_time: Option<String>,
}

#[derive(Deserialize, Debug, Clone)]
pub struct ShippingUpdateReq {
    pub id: String,
    pub software_id: String,
    pub customer_id: String,
    pub product_id: String,
    pub shipper: String,
    pub delivery_time: String,
    pub delivery_place: String,
    pub express_number: String,
    pub attachment: String,
    pub remarks: String,
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
    pub shipper: Option<String>,
    pub delivery_time: Option<String>,
    pub delivery_place: Option<String>,
    pub express_number: Option<String>,
}


