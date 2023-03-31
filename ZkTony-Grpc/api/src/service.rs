use chrono::NaiveDateTime;
use tonic::{Request, Response, Status};
use tonic_health::proto::health_server::{Health, HealthServer};

use application::{
    application_service_server::{ApplicationService, ApplicationServiceServer},
    Application, ApplicationId, ApplicationList, ApplicationPerPage, ApplicationReply,
    ApplicationSearch,
};
use entity::{
    application::Model as ApplicationModel, log::Model as LogModel,
    log_detail::Model as LogDetailModel, program::Model as ProgramModel,
};
use grpc_core::{sea_orm::DatabaseConnection, Mutation, Query};
use log::{
    log_service_server::{LogService, LogServiceServer},
    Log, LogId, LogList, LogPerPage, LogReply,
};
use log_detail::{
    log_detail_service_server::{LogDetailService, LogDetailServiceServer},
    LogDetail, LogDetailId, LogDetailList, LogDetailPerPage, LogDetailReply,
};
use program::{
    program_service_server::{ProgramService, ProgramServiceServer},
    Program, ProgramId, ProgramList, ProgramPerPage, ProgramReply,
};

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

impl Application {
    fn into_model(self) -> ApplicationModel {
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

impl Log {
    fn into_model(self) -> LogModel {
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

impl LogDetail {
    fn into_model(self) -> LogDetailModel {
        let create_time = NaiveDateTime::parse_from_str(&self.create_time, "%Y-%m-%d %H:%M:%S");
        LogDetailModel {
            id: self.id,
            log_id: self.log_id,
            content: self.content,
            create_time: create_time.ok(),
        }
    }
}

impl Program {
    fn into_model(self) -> ProgramModel {
        let create_time = NaiveDateTime::parse_from_str(&self.create_time, "%Y-%m-%d %H:%M:%S");
        ProgramModel {
            id: self.id,
            name: self.name,
            content: self.content,
            create_time: create_time.ok(),
        }
    }
}

#[derive(Debug, Default)]
pub struct MyApplicationServer {
    connection: DatabaseConnection,
}

#[derive(Debug, Default)]
pub struct MyLogServer {
    connection: DatabaseConnection,
}

#[derive(Debug, Default)]
pub struct MyLogDetailServer {
    connection: DatabaseConnection,
}

#[derive(Debug, Default)]
pub struct MyProgramServer {
    connection: DatabaseConnection,
}

#[tonic::async_trait]
impl ApplicationService for MyApplicationServer {
    #[tracing::instrument]
    async fn get_applications(
        &self,
        request: Request<ApplicationPerPage>,
    ) -> Result<Response<ApplicationList>, Status> {
        let conn = &self.connection;
        let per_page = request.into_inner().per_page;

        let mut response = ApplicationList {
            application: Vec::new(),
        };

        if let Ok((applications, _)) = Query::get_applications_in_page(conn, 1, per_page).await {
            for m in applications {
                response.application.push(Application {
                    id: m.id,
                    application_id: m.application_id,
                    build_type: m.build_type,
                    download_url: m.download_url,
                    version_name: m.version_name,
                    version_code: m.version_code,
                    description: m.description,
                    create_time: m.create_time.unwrap().to_string(),
                });
            }
            Ok(Response::new(response))
        } else {
            Err(Status::internal("Cannot find applications in page"))
        }
    }

    #[tracing::instrument]
    async fn get_by_application_id(
        &self,
        request: Request<ApplicationSearch>,
    ) -> Result<Response<Application>, Status> {
        let conn = &self.connection;
        let application_id = request.into_inner().application_id;

        if let Some(m) = Query::get_by_application_id(conn, application_id)
            .await
            .ok()
            .flatten()
        {
            Ok(Response::new(Application {
                id: m.id,
                application_id: m.application_id,
                build_type: m.build_type,
                download_url: m.download_url,
                version_name: m.version_name,
                version_code: m.version_code,
                description: m.description,
                create_time: m.create_time.unwrap().to_string(),
            }))
        } else {
            Err(Status::new(
                tonic::Code::Aborted,
                "Could not find Application ".to_string(),
            ))
        }
    }

    #[tracing::instrument]
    async fn get_by_id(
        &self,
        request: Request<ApplicationId>,
    ) -> Result<Response<Application>, Status> {
        let conn = &self.connection;
        let id = request.into_inner().id;

        if let Some(m) = Query::get_application_by_id(conn, id).await.ok().flatten() {
            Ok(Response::new(Application {
                id: m.id,
                application_id: m.application_id,
                build_type: m.build_type,
                download_url: m.download_url,
                version_name: m.version_name,
                version_code: m.version_code,
                description: m.description,
                create_time: m.create_time.unwrap().to_string(),
            }))
        } else {
            Err(Status::new(
                tonic::Code::Aborted,
                "Could not find Application ".to_owned(),
            ))
        }
    }

    #[tracing::instrument]
    async fn add_application(
        &self,
        request: Request<Application>,
    ) -> Result<Response<ApplicationId>, Status> {
        let conn = &self.connection;
        let input = request.into_inner().into_model();

        match Mutation::create_application(conn, input).await {
            Ok(m) => Ok(Response::new(ApplicationId { id: m.id })),
            Err(_) => Err(Status::new(
                tonic::Code::Aborted,
                "Could not find Application ".to_owned(),
            )),
        }
    }

    #[tracing::instrument]
    async fn update_application(
        &self,
        request: Request<Application>,
    ) -> Result<Response<ApplicationReply>, Status> {
        let conn = &self.connection;
        let input = request.into_inner().into_model();

        match Mutation::update_application(conn, input).await {
            Ok(_) => Ok(Response::new(ApplicationReply { success: true })),
            Err(_) => Ok(Response::new(ApplicationReply { success: false })),
        }
    }

    #[tracing::instrument]
    async fn delete_application(
        &self,
        request: Request<ApplicationId>,
    ) -> Result<Response<ApplicationReply>, Status> {
        let conn = &self.connection;
        let id = request.into_inner().id;

        match Mutation::delete_application(conn, id).await {
            Ok(_) => Ok(Response::new(ApplicationReply { success: true })),
            Err(_) => Ok(Response::new(ApplicationReply { success: false })),
        }
    }
}

#[tonic::async_trait]
impl LogService for MyLogServer {
    #[tracing::instrument]
    async fn get_logs(&self, request: Request<LogPerPage>) -> Result<Response<LogList>, Status> {
        let conn = &self.connection;
        let per_page = request.into_inner().per_page;

        let mut response = LogList { log: Vec::new() };

        if let Ok((logs, _)) = Query::get_logs_in_page(conn, 1, per_page).await {
            for m in logs {
                response.log.push(Log {
                    id: m.id,
                    sub_id: m.sub_id,
                    log_type: m.log_type,
                    content: m.content,
                    create_time: m.create_time.unwrap().to_string(),
                });
            }
            Ok(Response::new(response))
        } else {
            Err(Status::internal("Cannot find logs in page"))
        }
    }

    #[tracing::instrument]
    async fn get_by_id(&self, request: Request<LogId>) -> Result<Response<Log>, Status> {
        let conn = &self.connection;
        let id = request.into_inner().id;

        if let Some(m) = Query::get_log_by_id(conn, id).await.ok().flatten() {
            Ok(Response::new(Log {
                id: m.id,
                sub_id: m.sub_id,
                log_type: m.log_type,
                content: m.content,
                create_time: m.create_time.unwrap().to_string(),
            }))
        } else {
            Err(Status::new(
                tonic::Code::Aborted,
                "Could not find Log ".to_owned(),
            ))
        }
    }

    #[tracing::instrument]
    async fn add_log(&self, request: Request<Log>) -> Result<Response<LogId>, Status> {
        let conn = &self.connection;
        let input = request.into_inner().into_model();

        match Mutation::create_log(conn, input).await {
            Ok(m) => Ok(Response::new(LogId { id: m.id })),
            Err(_) => Err(Status::new(
                tonic::Code::Aborted,
                "Could not find Log ".to_owned(),
            )),
        }
    }

    #[tracing::instrument]
    async fn add_logs(&self, request: Request<LogList>) -> Result<Response<LogReply>, Status> {
        let conn = &self.connection;
        let mut input = Vec::new();
        for m in request.into_inner().log {
            input.push(m.into_model());
        }

        match Mutation::create_logs(conn, input).await {
            Ok(_) => Ok(Response::new(LogReply { success: true })),
            Err(_) => Ok(Response::new(LogReply { success: false })),
        }
    }

    #[tracing::instrument]
    async fn update_log(&self, request: Request<Log>) -> Result<Response<LogReply>, Status> {
        let conn = &self.connection;
        let input = request.into_inner().into_model();

        match Mutation::update_log(conn, input).await {
            Ok(_) => Ok(Response::new(LogReply { success: true })),
            Err(_) => Ok(Response::new(LogReply { success: false })),
        }
    }

    #[tracing::instrument]
    async fn delete_log(&self, request: Request<LogId>) -> Result<Response<LogReply>, Status> {
        let conn = &self.connection;
        let id = request.into_inner().id;

        match Mutation::delete_log(conn, id).await {
            Ok(_) => Ok(Response::new(LogReply { success: true })),
            Err(_) => Ok(Response::new(LogReply { success: false })),
        }
    }
}

#[tonic::async_trait]
impl LogDetailService for MyLogDetailServer {
    #[tracing::instrument]
    async fn get_log_details(
        &self,
        request: Request<LogDetailPerPage>,
    ) -> Result<Response<LogDetailList>, Status> {
        let conn = &self.connection;
        let per_page = request.into_inner().per_page;

        let mut response = LogDetailList {
            log_detail: Vec::new(),
        };

        if let Ok((log_details, _)) = Query::get_log_details_in_page(conn, 1, per_page).await {
            for m in log_details {
                response.log_detail.push(LogDetail {
                    id: m.id,
                    log_id: m.log_id,
                    content: m.content,
                    create_time: m.create_time.unwrap().to_string(),
                });
            }
            Ok(Response::new(response))
        } else {
            Err(Status::internal("Cannot find log_details in page"))
        }
    }

    #[tracing::instrument]
    async fn get_by_id(
        &self,
        request: Request<LogDetailId>,
    ) -> Result<Response<LogDetail>, Status> {
        let conn = &self.connection;
        let id = request.into_inner().id;

        if let Some(m) = Query::get_log_detail_by_id(conn, id).await.ok().flatten() {
            Ok(Response::new(LogDetail {
                id: m.id,
                log_id: m.log_id,
                content: m.content,
                create_time: m.create_time.unwrap().to_string(),
            }))
        } else {
            Err(Status::new(
                tonic::Code::Aborted,
                "Could not find LogDetail ".to_owned(),
            ))
        }
    }

    #[tracing::instrument]
    async fn add_log_detail(
        &self,
        request: Request<LogDetail>,
    ) -> Result<Response<LogDetailId>, Status> {
        let conn = &self.connection;
        let input = request.into_inner().into_model();

        match Mutation::create_log_detail(conn, input).await {
            Ok(m) => Ok(Response::new(LogDetailId { id: m.id })),
            Err(_) => Err(Status::new(
                tonic::Code::Aborted,
                "Could not find LogDetail ".to_owned(),
            )),
        }
    }

    #[tracing::instrument]
    async fn add_log_details(
        &self,
        request: Request<LogDetailList>,
    ) -> Result<Response<LogDetailReply>, Status> {
        let conn = &self.connection;
        let mut input = Vec::new();
        for m in request.into_inner().log_detail {
            input.push(m.into_model())
        }

        match Mutation::create_log_details(conn, input).await {
            Ok(_) => Ok(Response::new(LogDetailReply { success: true })),
            Err(_) => Ok(Response::new(LogDetailReply { success: false })),
        }
    }

    #[tracing::instrument]
    async fn update_log_detail(
        &self,
        request: Request<LogDetail>,
    ) -> Result<Response<LogDetailReply>, Status> {
        let conn = &self.connection;
        let input = request.into_inner().into_model();

        match Mutation::update_log_detail(conn, input).await {
            Ok(_) => Ok(Response::new(LogDetailReply { success: true })),
            Err(_) => Ok(Response::new(LogDetailReply { success: false })),
        }
    }

    #[tracing::instrument]
    async fn delete_log_detail(
        &self,
        request: Request<LogDetailId>,
    ) -> Result<Response<LogDetailReply>, Status> {
        let conn = &self.connection;
        let id = request.into_inner().id;

        match Mutation::delete_log_detail(conn, id).await {
            Ok(_) => Ok(Response::new(LogDetailReply { success: true })),
            Err(_) => Ok(Response::new(LogDetailReply { success: false })),
        }
    }
}

#[tonic::async_trait]
impl ProgramService for MyProgramServer {
    #[tracing::instrument]
    async fn get_programs(
        &self,
        request: Request<ProgramPerPage>,
    ) -> Result<Response<ProgramList>, Status> {
        let conn = &self.connection;
        let per_page = request.into_inner().per_page;

        let mut response = ProgramList {
            program: Vec::new(),
        };

        if let Ok((programs, _)) = Query::get_programs_in_page(conn, 1, per_page).await {
            for m in programs {
                response.program.push(Program {
                    id: m.id,
                    name: m.name,
                    content: m.content,
                    create_time: m.create_time.unwrap().to_string(),
                });
            }
            Ok(Response::new(response))
        } else {
            Err(Status::internal("Cannot find programs in page"))
        }
    }

    #[tracing::instrument]
    async fn get_by_id(&self, request: Request<ProgramId>) -> Result<Response<Program>, Status> {
        let conn = &self.connection;
        let id = request.into_inner().id;

        if let Some(m) = Query::get_program_by_id(conn, id).await.ok().flatten() {
            Ok(Response::new(Program {
                id: m.id,
                name: m.name,
                content: m.content,
                create_time: m.create_time.unwrap().to_string(),
            }))
        } else {
            Err(Status::new(
                tonic::Code::Aborted,
                "Could not find Program ".to_owned(),
            ))
        }
    }

    #[tracing::instrument]
    async fn add_program(&self, request: Request<Program>) -> Result<Response<ProgramId>, Status> {
        let conn = &self.connection;
        let input = request.into_inner().into_model();

        match Mutation::create_program(conn, input).await {
            Ok(m) => Ok(Response::new(ProgramId { id: m.id })),
            Err(_) => Err(Status::new(
                tonic::Code::Aborted,
                "Could not find Program ".to_owned(),
            )),
        }
    }

    #[tracing::instrument]
    async fn add_programs(
        &self,
        request: Request<ProgramList>,
    ) -> Result<Response<ProgramReply>, Status> {
        let conn = &self.connection;
        let mut input = Vec::new();
        for m in request.into_inner().program {
            input.push(m.into_model())
        }

        match Mutation::create_programs(conn, input).await {
            Ok(_) => Ok(Response::new(ProgramReply { success: true })),
            Err(_) => Ok(Response::new(ProgramReply { success: false })),
        }
    }

    #[tracing::instrument]
    async fn update_program(
        &self,
        request: Request<Program>,
    ) -> Result<Response<ProgramReply>, Status> {
        let conn = &self.connection;
        let input = request.into_inner().into_model();

        match Mutation::update_program(conn, input).await {
            Ok(_) => Ok(Response::new(ProgramReply { success: true })),
            Err(_) => Ok(Response::new(ProgramReply { success: false })),
        }
    }

    #[tracing::instrument]
    async fn delete_program(
        &self,
        request: Request<ProgramId>,
    ) -> Result<Response<ProgramReply>, Status> {
        let conn = &self.connection;
        let id = request.into_inner().id;

        match Mutation::delete_program(conn, id).await {
            Ok(_) => Ok(Response::new(ProgramReply { success: true })),
            Err(_) => Ok(Response::new(ProgramReply { success: false })),
        }
    }
}

pub fn application_svc(
    connection: DatabaseConnection,
) -> ApplicationServiceServer<MyApplicationServer> {
    ApplicationServiceServer::new(MyApplicationServer { connection })
}

pub fn program_svc(connection: DatabaseConnection) -> ProgramServiceServer<MyProgramServer> {
    ProgramServiceServer::new(MyProgramServer { connection })
}

pub fn log_svc(connection: DatabaseConnection) -> LogServiceServer<MyLogServer> {
    LogServiceServer::new(MyLogServer { connection })
}

pub fn log_detail_svc(connection: DatabaseConnection) -> LogDetailServiceServer<MyLogDetailServer> {
    LogDetailServiceServer::new(MyLogDetailServer { connection })
}

pub async fn health_svc() -> HealthServer<impl Health> {
    let (mut health_reporter, health_service) = tonic_health::server::health_reporter();
    health_reporter
        .set_serving::<ApplicationServiceServer<MyApplicationServer>>()
        .await;

    health_reporter
        .set_serving::<ProgramServiceServer<MyProgramServer>>()
        .await;

    health_reporter
        .set_serving::<LogServiceServer<MyLogServer>>()
        .await;

    health_reporter
        .set_serving::<LogDetailServiceServer<MyLogDetailServer>>()
        .await;

    health_service
}
