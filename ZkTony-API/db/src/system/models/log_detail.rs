use serde::Deserialize;

#[derive(Deserialize, Debug, Clone)]
pub struct LogDetailAddReq {
    pub id: String,
    pub log_id: String,
    pub content: String,
    pub create_time: Option<String>,
}
