use chrono::NaiveDateTime;

use entity::{
    application::Model as ApplicationModel, log::Model as LogModel,
    log_detail::Model as LogDetailModel, program::Model as ProgramModel,
};

use application::{Application, ApplicationList, ApplicationRequestPage};
use log::{Log, LogList, LogRequestPage};
use log_detail::{LogDetail, LogDetailList, LogDetailRequestPage};
use program::{Program, ProgramList, ProgramRequestPage};

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
        let create_time = NaiveDateTime::parse_from_str(&self.create_time, "%Y-%m-%d %H:%M:%S");
        ApplicationModel {
            id: self.id,
            application_id: self.application_id,
            build_type: self.build_type,
            download_url: self.download_url,
            version_name: self.version_name,
            version_code: self.version_code,
            description: self.description,
            create_time: create_time.ok(),
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

impl ApplicationRequestPage {
    pub fn into_page(self) -> (u64, u64) {
        (self.page, self.page_size)
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
            create_time: model.create_time.unwrap().to_string(),
        }
    }
}

impl Log {
    pub fn into_model(self) -> LogModel {
        let create_time = NaiveDateTime::parse_from_str(&self.create_time, "%Y-%m-%d %H:%M:%S");
        LogModel {
            id: self.id,
            sub_id: self.sub_id,
            log_type: self.log_type,
            content: self.content,
            create_time: create_time.ok(),
        }
    }
}

impl LogList {
    pub fn into_models(self) -> Vec<LogModel> {
        self.list.into_iter().map(|log| log.into_model()).collect()
    }
}

impl LogRequestPage {
    pub fn into_page(self) -> (u64, u64) {
        (self.page, self.page_size)
    }
}

impl From<LogModel> for Log {
    fn from(model: LogModel) -> Self {
        Log {
            id: model.id,
            sub_id: model.sub_id,
            log_type: model.log_type,
            content: model.content,
            create_time: model.create_time.unwrap().to_string(),
        }
    }
}

impl LogDetail {
    pub fn into_model(self) -> LogDetailModel {
        let create_time = NaiveDateTime::parse_from_str(&self.create_time, "%Y-%m-%d %H:%M:%S");
        LogDetailModel {
            id: self.id,
            log_id: self.log_id,
            content: self.content,
            create_time: create_time.ok(),
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

impl LogDetailRequestPage {
    pub fn into_page(self) -> (u64, u64) {
        (self.page, self.page_size)
    }
}

impl From<LogDetailModel> for LogDetail {
    fn from(model: LogDetailModel) -> Self {
        LogDetail {
            id: model.id,
            log_id: model.log_id,
            content: model.content,
            create_time: model.create_time.unwrap().to_string(),
        }
    }
}

impl Program {
    pub fn into_model(self) -> ProgramModel {
        let create_time = NaiveDateTime::parse_from_str(&self.create_time, "%Y-%m-%d %H:%M:%S");
        ProgramModel {
            id: self.id,
            name: self.name,
            content: self.content,
            create_time: create_time.ok(),
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

impl ProgramRequestPage {
    pub fn into_page(self) -> (u64, u64) {
        (self.page, self.page_size)
    }
}

impl From<ProgramModel> for Program {
    fn from(model: ProgramModel) -> Self {
        Program {
            id: model.id,
            name: model.name,
            content: model.content,
            create_time: model.create_time.unwrap().to_string(),
        }
    }
}
