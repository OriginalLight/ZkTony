use serde::Deserialize;

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
