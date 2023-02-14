use serde::Deserialize;

#[derive(Deserialize, Debug, Clone)]
pub struct LogAddReq {
    pub id: String,
    pub sub_id: String,
    pub log_type: String,
    pub content: String,
    pub create_time: Option<String>,
}
