use serde::Deserialize;

#[derive(Deserialize, Debug, Clone)]
#[serde(rename_all = "camelCase")]
pub struct LogDataAddReq {
    pub id: String,
    pub log_id: String,
    pub motor: i32,
    pub voltage: f64,
    pub current: f64,
    pub time: f64,
    pub upload: i32,
    pub create_time: Option<String>,
}
