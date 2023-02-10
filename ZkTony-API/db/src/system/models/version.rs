use serde::Deserialize;

#[derive(Deserialize, Clone)]
pub struct VersionDbSearchReq {
    pub id: i32,
}