use serde::Deserialize;

#[derive(Deserialize, Clone)]
#[serde(rename_all = "camelCase")]
pub struct VersionDbSearchReq {
    pub id: i32,
}
