use serde::Deserialize;

// region: common
#[derive(Deserialize, Clone)]
pub struct DeleteReq {
    pub id: String,
}
// endregion

// region: application
#[derive(Deserialize, Clone)]
pub struct ApplicationSearchReq {
    pub application_id: String,
}
// endregion

// region: customer
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
// endregion

// region: equipment
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
// endregion

// region: log_detail
#[derive(Deserialize, Debug, Clone)]
pub struct LogDetailAddReq {
    pub id: String,
    pub log_id: String,
    pub content: String,
    pub create_time: Option<String>,
}
// endregion

// region: log
#[derive(Deserialize, Debug, Clone)]
pub struct LogAddReq {
    pub id: String,
    pub sub_id: String,
    pub log_type: String,
    pub content: String,
    pub create_time: Option<String>,
}
// endregion

// region: product
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
// endregion

// region: program
#[derive(Deserialize, Debug, Clone)]
pub struct ProgramAddReq {
    pub id: String,
    pub name: String,
    pub content: String,
    pub create_time: Option<String>,
}
// endregion

// region: software

#[derive(Deserialize, Debug, Clone)]
pub struct SoftwareSaveReq {
    pub id: String,
    pub package: String,
    pub version_code: i32,
    pub version_name: String,
    pub build_type: String,
    pub remarks: Option<String>,
    pub create_by: Option<String>,
    pub create_time: Option<String>,
}

#[derive(Deserialize, Debug, Clone)]
pub struct SoftwareDeleteReq {
    pub id: String,
}

#[derive(Deserialize, Debug, Clone)]
pub struct SoftwareGetReq {
    pub id: Option<String>,
    pub package: Option<String>,
    pub build_type: Option<String>,
}
// endregion
