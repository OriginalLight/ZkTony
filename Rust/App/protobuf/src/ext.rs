use chrono::NaiveDateTime;

use entity::{
    form::{ApplicationQueryForm, LogDetailQueryForm, LogQueryForm, ProgramQueryForm},
    prelude::{ApplicationModel, LogDetailModel, LogModel, ProgramModel},
};

use application::*;
use log::*;
use log_detail::*;
use program::*;

pub mod application {
    tonic::include_proto!("application");
}

pub mod log {
    tonic::include_proto!("log");
}

pub mod log_detail {
    tonic::include_proto!("log_detail");
}

pub mod program {
    tonic::include_proto!("program");
}

pub mod test {
    tonic::include_proto!("test");
}

impl Application {
    pub fn into_model(self) -> ApplicationModel {
        ApplicationModel {
            id: self.id,
            application_id: self.application_id,
            build_type: self.build_type,
            download_url: self.download_url,
            version_name: self.version_name,
            version_code: self.version_code,
            description: self.description,
            create_time: self.create_time.to_naivedatetime(),
        }
    }
}

impl ApplicationRequestQuery {
    pub fn into_query(self) -> ApplicationQueryForm {
        ApplicationQueryForm {
            page_size: self.page_size,
            page: self.page,
            id: self.id,
            application_id: self.application_id,
            build_type: self.build_type,
        }
    }
}

impl ApplicationList {
    pub fn into_models(self) -> Vec<ApplicationModel> {
        self.list
            .into_iter()
            .map(|application| application.into_model())
            .collect()
    }
}

impl From<ApplicationModel> for Application {
    fn from(model: ApplicationModel) -> Self {
        Application {
            id: model.id,
            application_id: model.application_id,
            build_type: model.build_type,
            download_url: model.download_url,
            version_name: model.version_name,
            version_code: model.version_code,
            description: model.description,
            create_time: model.create_time.to_string(),
        }
    }
}

impl Log {
    pub fn into_model(self) -> LogModel {
        LogModel {
            id: self.id,
            sub_id: self.sub_id,
            log_type: self.log_type,
            content: self.content,
            create_time: self.create_time.to_naivedatetime(),
        }
    }
}

impl LogList {
    pub fn into_models(self) -> Vec<LogModel> {
        self.list.into_iter().map(|log| log.into_model()).collect()
    }
}

impl LogRequestQuery {
    pub fn into_query(self) -> LogQueryForm {
        LogQueryForm {
            page_size: self.page_size,
            page: self.page,
            id: self.id,
            sub_id: self.sub_id,
            log_type: self.log_type,
        }
    }
}

impl From<LogModel> for Log {
    fn from(model: LogModel) -> Self {
        Log {
            id: model.id,
            sub_id: model.sub_id,
            log_type: model.log_type,
            content: model.content,
            create_time: model.create_time.to_string(),
        }
    }
}

impl LogDetail {
    pub fn into_model(self) -> LogDetailModel {
        LogDetailModel {
            id: self.id,
            log_id: self.log_id,
            content: self.content,
            create_time: self.create_time.to_naivedatetime(),
        }
    }
}

impl LogDetailList {
    pub fn into_models(self) -> Vec<LogDetailModel> {
        self.list
            .into_iter()
            .map(|log_detail| log_detail.into_model())
            .collect()
    }
}

impl LogDetailRequestQuery {
    pub fn into_query(self) -> LogDetailQueryForm {
        LogDetailQueryForm {
            page_size: self.page_size,
            page: self.page,
            id: self.id,
        }
    }
}

impl From<LogDetailModel> for LogDetail {
    fn from(model: LogDetailModel) -> Self {
        LogDetail {
            id: model.id,
            log_id: model.log_id,
            content: model.content,
            create_time: model.create_time.to_string(),
        }
    }
}

impl Program {
    pub fn into_model(self) -> ProgramModel {
        ProgramModel {
            id: self.id,
            name: self.name,
            content: self.content,
            create_time: self.create_time.to_naivedatetime(),
        }
    }
}

impl ProgramList {
    pub fn into_models(self) -> Vec<ProgramModel> {
        self.list
            .into_iter()
            .map(|program| program.into_model())
            .collect()
    }
}

impl ProgramRequestQuery {
    pub fn into_query(self) -> ProgramQueryForm {
        ProgramQueryForm {
            page_size: self.page_size,
            page: self.page,
            id: self.id,
            name: self.name,
        }
    }
}

impl From<ProgramModel> for Program {
    fn from(model: ProgramModel) -> Self {
        Program {
            id: model.id,
            name: model.name,
            content: model.content,
            create_time: model.create_time.to_string(),
        }
    }
}

// String -> Option<NaiveDateTime>
pub trait StringExt {
    fn to_naivedatetime(&self) -> Option<NaiveDateTime>;
}

impl StringExt for String {
    fn to_naivedatetime(&self) -> Option<NaiveDateTime> {
        NaiveDateTime::parse_from_str(self, "%Y-%m-%d %H:%M:%S").ok()
    }
}

// Option<NaiveDateTime> -> String
pub trait NaiveDateTimeExt {
    fn to_string(&self) -> String;
}

impl NaiveDateTimeExt for Option<NaiveDateTime> {
    fn to_string(&self) -> String {
        match self {
            Some(time) => time.format("%Y-%m-%d %H:%M:%S").to_string(),
            None => "".to_string(),
        }
    }
}
