#[derive(Clone, Debug)]
pub struct ApplicationQueryForm {
    pub page_size: u64,
    pub page: u64,
    pub id: i32,
    pub application_id: String,
    pub build_type: String,
}

#[derive(Clone, Debug)]
pub struct LogQueryForm {
    pub page_size: u64,
    pub page: u64,
    pub id: String,
    pub sub_id: String,
    pub log_type: String,
}

#[derive(Clone, Debug)]
pub struct LogDetailQueryForm {
    pub page_size: u64,
    pub page: u64,
    pub id: String,
}

#[derive(Clone, Debug)]
pub struct ProgramQueryForm {
    pub page_size: u64,
    pub page: u64,
    pub id: String,
    pub name: String,
}
