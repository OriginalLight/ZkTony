use serde::Deserialize;

#[derive(Deserialize, Debug, Clone)]
pub struct SoftwareAddReq {
    pub id: String,
    pub package: String,
    pub version_code: String,
    pub version_name: String,
    pub build_type: String,
    pub remarks: i32,
    pub create_time: Option<String>,
}

#[derive(Deserialize, Debug, Clone)]
pub struct SoftwareUpdateReq {
    pub id: String,
    pub package: String,
    pub version_code: String,
    pub version_name: String,
    pub build_type: String,
    pub remarks: i32,
}

#[derive(Deserialize, Debug, Clone)]
pub struct SoftwareDeleteReq {
    pub id: String,
}

#[derive(Deserialize, Debug, Clone)]
pub struct SoftwareGetReq {
    pub id: Option<String>,
    pub package: Option<String>,
    pub version_code: Option<String>,
    pub version_name: Option<String>,
    pub build_type: Option<String>,
}
