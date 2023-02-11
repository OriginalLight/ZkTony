use serde::Deserialize;

#[derive(Deserialize, Clone)]
pub struct ApplicationSearchReq {
    pub application_id: String,
}
