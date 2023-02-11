use serde::Deserialize;

#[derive(Deserialize, Debug, Clone)]
#[serde(rename_all = "camelCase")]
pub struct LogAddReq {
    pub id: String,
    pub program_id: String,
    pub motor: i32,
    pub voltage: f64,
    pub time: f64,
    pub model: i32,
    pub upload: i32,
    pub create_time: Option<String>,
}
