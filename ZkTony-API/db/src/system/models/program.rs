use serde::Deserialize;

#[derive(Deserialize, Debug, Clone)]
pub struct ProgramAddReq {
    pub id: String,
    pub name: String,
    pub content: String,
    pub create_time: Option<String>,
}
