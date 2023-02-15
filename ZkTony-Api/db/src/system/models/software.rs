use serde::Deserialize;

#[derive(Deserialize, Debug, Clone)]
pub struct SoftwareSaveReq {
    pub id: String,
    pub package: String,
    pub version_code: i32,
    pub version_name: String,
    pub build_type: String,
    pub remarks: String,
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
    pub version_code: Option<i32>,
    pub version_name: Option<String>,
    pub build_type: Option<String>,
}
