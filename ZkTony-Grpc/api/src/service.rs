use super::protobuf::application::{
    application_service_server::{ApplicationService, ApplicationServiceServer},
    Application, ApplicationId, ApplicationReply, ApplicationReplyPage, ApplicationRequestPage,
    ApplicationSearch,
};

use grpc_core::{sea_orm::DatabaseConnection, Mutation, Query};
use tonic::{Request, Response, Status};
use tonic_health::proto::health_server::{Health, HealthServer};

use super::protobuf::log::{
    log_service_server::{LogService, LogServiceServer},
    Log, LogId, LogList, LogReply, LogReplyPage, LogRequestPage,
};
use super::protobuf::log_detail::{
    log_detail_service_server::{LogDetailService, LogDetailServiceServer},
    LogDetail, LogDetailId, LogDetailList, LogDetailReply, LogDetailReplyPage,
    LogDetailRequestPage,
};
use super::protobuf::program::{
    program_service_server::{ProgramService, ProgramServiceServer},
    Program, ProgramId, ProgramList, ProgramReply, ProgramReplyPage, ProgramRequestPage,
};

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
        request: Request<ApplicationRequestPage>,
    ) -> Result<Response<ApplicationReplyPage>, Status> {
        let conn = &self.connection;
        let (page, page_size) = request.into_inner().into_page();

        if let Ok((applications, total)) =
            Query::get_applications_in_page(conn, page, page_size).await
        {
            Ok(Response::new(ApplicationReplyPage {
                list: applications
                    .iter()
                    .map(|model| Application::from(model.clone()))
                    .collect::<Vec<Application>>(),
                total,
            }))
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

        if let Some(application) = Query::get_by_application_id(conn, application_id)
            .await
            .ok()
            .flatten()
        {
            Ok(Response::new(Application::from(application)))
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

        if let Some(application) = Query::get_application_by_id(conn, id).await.ok().flatten() {
            Ok(Response::new(Application::from(application)))
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
    async fn get_logs(
        &self,
        request: Request<LogRequestPage>,
    ) -> Result<Response<LogReplyPage>, Status> {
        let conn = &self.connection;
        let (page, page_size) = request.into_inner().into_page();

        if let Ok((logs, total)) = Query::get_logs_in_page(conn, page, page_size).await {
            Ok(Response::new(LogReplyPage {
                list: logs
                    .iter()
                    .map(|model| Log::from(model.clone()))
                    .collect::<Vec<Log>>(),
                total,
            }))
        } else {
            Err(Status::internal("Cannot find logs in page"))
        }
    }

    #[tracing::instrument]
    async fn get_by_id(&self, request: Request<LogId>) -> Result<Response<Log>, Status> {
        let conn = &self.connection;
        let id = request.into_inner().id;

        if let Some(log) = Query::get_log_by_id(conn, id).await.ok().flatten() {
            Ok(Response::new(Log::from(log)))
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
        let input = request.into_inner().into_models();

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
        request: Request<LogDetailRequestPage>,
    ) -> Result<Response<LogDetailReplyPage>, Status> {
        let conn = &self.connection;
        let (page, page_size) = request.into_inner().into_page();

        if let Ok((models, total)) = Query::get_log_details_in_page(conn, page, page_size).await {
            Ok(Response::new(LogDetailReplyPage {
                list: models
                    .iter()
                    .map(|model| LogDetail::from(model.clone()))
                    .collect::<Vec<LogDetail>>(),
                total,
            }))
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

        if let Some(model) = Query::get_log_detail_by_id(conn, id).await.ok().flatten() {
            Ok(Response::new(LogDetail::from(model)))
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
        let input = request.into_inner().into_models();

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
        request: Request<ProgramRequestPage>,
    ) -> Result<Response<ProgramReplyPage>, Status> {
        let conn = &self.connection;
        let (page, page_size) = request.into_inner().into_page();

        if let Ok((models, total)) = Query::get_programs_in_page(conn, page, page_size).await {
            Ok(Response::new(ProgramReplyPage {
                list: models
                    .iter()
                    .map(|model| Program::from(model.clone()))
                    .collect::<Vec<Program>>(),
                total,
            }))
        } else {
            Err(Status::internal("Cannot find programs in page"))
        }
    }

    #[tracing::instrument]
    async fn get_by_id(&self, request: Request<ProgramId>) -> Result<Response<Program>, Status> {
        let conn = &self.connection;
        let id = request.into_inner().id;

        if let Some(model) = Query::get_program_by_id(conn, id).await.ok().flatten() {
            Ok(Response::new(Program::from(model)))
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
        let input = request.into_inner().into_models();

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
