use grpc_core::{sea_orm::DbConn, *};

use tonic::{
    codec::CompressionEncoding,
    transport::{server::Router, Server},
    Code, Request, Response, Status,
};

use super::config::CFG;

use super::protobuf::{
    application::{application_service_server::*, *},
    log::{log_service_server::*, *},
    log_detail::{log_detail_service_server::*, *},
    program::{program_service_server::*, *},
    test::{test_service_server::*, *},
};

#[derive(Debug, Default, Clone)]
pub struct MyApplicationServer {
    db_conn: DbConn,
}

#[derive(Debug, Default, Clone)]
pub struct MyLogServer {
    db_conn: DbConn,
}

#[derive(Debug, Default, Clone)]
pub struct MyLogDetailServer {
    db_conn: DbConn,
}

#[derive(Debug, Default, Clone)]
pub struct MyProgramServer {
    db_conn: DbConn,
}

#[derive(Debug, Default, Clone)]
pub struct MyTestServer {}

#[tonic::async_trait]
impl ApplicationService for MyApplicationServer {
    #[tracing::instrument]
    async fn get_applications(
        &self,
        request: Request<ApplicationRequestPage>,
    ) -> Result<Response<ApplicationReplyPage>, Status> {
        let conn = &self.db_conn;
        let (page, page_size) = request.into_inner().into_page();

        if page == 0 || page_size == 0 {
            return Err(Status::invalid_argument(
                "Page and page size cannot be zero",
            ));
        }

        match ApplicationQuery::get_applications_in_page(conn, page, page_size).await {
            Ok((models, total)) => Ok(Response::new(ApplicationReplyPage {
                list: models
                    .iter()
                    .map(|model| Application::from(model.clone()))
                    .collect::<Vec<Application>>(),
                total,
            })),
            Err(e) => Err(Status::internal(e.to_string())),
        }
    }

    #[tracing::instrument]
    async fn get_by_application_id(
        &self,
        request: Request<ApplicationSearch>,
    ) -> Result<Response<Application>, Status> {
        let conn = &self.db_conn;
        let application_id = request.into_inner().application_id;

        if application_id.is_empty() {
            return Err(Status::invalid_argument("Application id cannot be empty"));
        }

        match ApplicationQuery::get_by_application_id(conn, application_id).await {
            Ok(model) => match model {
                Some(m) => Ok(Response::new(Application::from(m))),
                None => Err(Status::not_found("Cannot find application")),
            },
            Err(e) => Err(Status::internal(e.to_string())),
        }
    }

    #[tracing::instrument]
    async fn get_by_id(
        &self,
        request: Request<ApplicationId>,
    ) -> Result<Response<Application>, Status> {
        let conn = &self.db_conn;
        let id = request.into_inner().id;

        match ApplicationQuery::get_application_by_id(conn, id).await {
            Ok(model) => match model {
                Some(m) => Ok(Response::new(Application::from(m))),
                None => Err(Status::not_found("Cannot find application")),
            },
            Err(e) => Err(Status::internal(e.to_string())),
        }
    }

    #[tracing::instrument]
    async fn add_application(
        &self,
        request: Request<Application>,
    ) -> Result<Response<ApplicationId>, Status> {
        let conn = &self.db_conn;
        let input = request.into_inner().into_model();

        match ApplicationMutation::create_application(conn, input).await {
            Ok(m) => Ok(Response::new(ApplicationId { id: m.id })),
            Err(e) => Err(Status::internal(e.to_string())),
        }
    }

    #[tracing::instrument]
    async fn update_application(
        &self,
        request: Request<Application>,
    ) -> Result<Response<ApplicationReply>, Status> {
        let conn = &self.db_conn;
        let input = request.into_inner().into_model();

        match ApplicationMutation::update_application(conn, input).await {
            Ok(_) => Ok(Response::new(ApplicationReply { success: true })),
            Err(_) => Ok(Response::new(ApplicationReply { success: false })),
        }
    }

    #[tracing::instrument]
    async fn delete_application(
        &self,
        request: Request<ApplicationId>,
    ) -> Result<Response<ApplicationReply>, Status> {
        let conn = &self.db_conn;
        let id = request.into_inner().id;

        match ApplicationMutation::delete_application(conn, id).await {
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
        let conn = &self.db_conn;
        let (page, page_size) = request.into_inner().into_page();

        if page == 0 || page_size == 0 {
            return Err(Status::invalid_argument(
                "Page and page size cannot be zero",
            ));
        }

        match LogQuery::get_logs_in_page(conn, page, page_size).await {
            Ok((models, total)) => Ok(Response::new(LogReplyPage {
                list: models
                    .iter()
                    .map(|model| Log::from(model.clone()))
                    .collect::<Vec<Log>>(),
                total,
            })),
            Err(e) => Err(Status::internal(e.to_string())),
        }
    }

    #[tracing::instrument]
    async fn get_by_id(&self, request: Request<LogId>) -> Result<Response<Log>, Status> {
        let conn = &self.db_conn;
        let id = request.into_inner().id;

        match LogQuery::get_log_by_id(conn, id).await {
            Ok(model) => match model {
                Some(m) => Ok(Response::new(Log::from(m))),
                None => Err(Status::not_found("Cannot find log")),
            },
            Err(e) => Err(Status::internal(e.to_string())),
        }
    }

    #[tracing::instrument]
    async fn add_log(&self, request: Request<Log>) -> Result<Response<LogId>, Status> {
        let conn = &self.db_conn;
        let input = request.into_inner().into_model();

        match LogMutation::create_log(conn, input).await {
            Ok(m) => Ok(Response::new(LogId { id: m.id })),
            Err(e) => Err(Status::internal(e.to_string())),
        }
    }

    #[tracing::instrument]
    async fn add_logs(&self, request: Request<LogList>) -> Result<Response<LogReply>, Status> {
        let conn = &self.db_conn;
        let input = request.into_inner().into_models();

        match LogMutation::create_logs(conn, input).await {
            Ok(_) => Ok(Response::new(LogReply { success: true })),
            Err(_) => Ok(Response::new(LogReply { success: false })),
        }
    }

    #[tracing::instrument]
    async fn update_log(&self, request: Request<Log>) -> Result<Response<LogReply>, Status> {
        let conn = &self.db_conn;
        let input = request.into_inner().into_model();

        match LogMutation::update_log(conn, input).await {
            Ok(_) => Ok(Response::new(LogReply { success: true })),
            Err(_) => Ok(Response::new(LogReply { success: false })),
        }
    }

    #[tracing::instrument]
    async fn delete_log(&self, request: Request<LogId>) -> Result<Response<LogReply>, Status> {
        let conn = &self.db_conn;
        let id = request.into_inner().id;

        match LogMutation::delete_log(conn, id).await {
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
        let conn = &self.db_conn;
        let (page, page_size) = request.into_inner().into_page();

        if page == 0 || page_size == 0 {
            return Err(Status::invalid_argument(
                "Page and page size cannot be zero",
            ));
        }
        match LogDetailQuery::get_log_details_in_page(conn, page, page_size).await {
            Ok((models, total)) => Ok(Response::new(LogDetailReplyPage {
                list: models
                    .iter()
                    .map(|model| LogDetail::from(model.clone()))
                    .collect::<Vec<LogDetail>>(),
                total,
            })),
            Err(e) => Err(Status::internal(e.to_string())),
        }
    }

    #[tracing::instrument]
    async fn get_by_id(
        &self,
        request: Request<LogDetailId>,
    ) -> Result<Response<LogDetail>, Status> {
        let conn = &self.db_conn;
        let id = request.into_inner().id;

        match LogDetailQuery::get_log_detail_by_id(conn, id).await {
            Ok(model) => match model {
                Some(m) => Ok(Response::new(LogDetail::from(m))),
                None => Err(Status::not_found("Cannot find log detail")),
            },
            Err(e) => Err(Status::internal(e.to_string())),
        }
    }

    #[tracing::instrument]
    async fn add_log_detail(
        &self,
        request: Request<LogDetail>,
    ) -> Result<Response<LogDetailId>, Status> {
        let conn = &self.db_conn;
        let input = request.into_inner().into_model();

        match LogDetailMutation::create_log_detail(conn, input).await {
            Ok(m) => Ok(Response::new(LogDetailId { id: m.id })),
            Err(e) => Err(Status::new(tonic::Code::Aborted, e.to_string().to_owned())),
        }
    }

    #[tracing::instrument]
    async fn add_log_details(
        &self,
        request: Request<LogDetailList>,
    ) -> Result<Response<LogDetailReply>, Status> {
        let conn = &self.db_conn;
        let input = request.into_inner().into_models();

        match LogDetailMutation::create_log_details(conn, input).await {
            Ok(_) => Ok(Response::new(LogDetailReply { success: true })),
            Err(_) => Ok(Response::new(LogDetailReply { success: false })),
        }
    }

    #[tracing::instrument]
    async fn update_log_detail(
        &self,
        request: Request<LogDetail>,
    ) -> Result<Response<LogDetailReply>, Status> {
        let conn = &self.db_conn;
        let input = request.into_inner().into_model();

        match LogDetailMutation::update_log_detail(conn, input).await {
            Ok(_) => Ok(Response::new(LogDetailReply { success: true })),
            Err(_) => Ok(Response::new(LogDetailReply { success: false })),
        }
    }

    #[tracing::instrument]
    async fn delete_log_detail(
        &self,
        request: Request<LogDetailId>,
    ) -> Result<Response<LogDetailReply>, Status> {
        let conn = &self.db_conn;
        let id = request.into_inner().id;

        match LogDetailMutation::delete_log_detail(conn, id).await {
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
        let conn = &self.db_conn;
        let (page, page_size) = request.into_inner().into_page();

        if page == 0 || page_size == 0 {
            return Err(Status::new(
                Code::InvalidArgument,
                "Page and page size cannot be 0".to_string(),
            ));
        }

        match ProgramQuery::get_programs_in_page(conn, page, page_size).await {
            Ok((models, total)) => Ok(Response::new(ProgramReplyPage {
                list: models
                    .iter()
                    .map(|model| Program::from(model.clone()))
                    .collect::<Vec<Program>>(),
                total,
            })),
            Err(e) => Err(Status::internal(e.to_string())),
        }
    }

    #[tracing::instrument]
    async fn get_by_id(&self, request: Request<ProgramId>) -> Result<Response<Program>, Status> {
        let conn = &self.db_conn;
        let id = request.into_inner().id;

        match ProgramQuery::get_program_by_id(conn, id).await {
            Ok(model) => match model {
                Some(m) => Ok(Response::new(Program::from(m))),
                None => Err(Status::not_found("Cannot find program")),
            },
            Err(e) => Err(Status::internal(e.to_string())),
        }
    }

    #[tracing::instrument]
    async fn add_program(&self, request: Request<Program>) -> Result<Response<ProgramId>, Status> {
        let conn = &self.db_conn;
        let input = request.into_inner().into_model();

        match ProgramMutation::create_program(conn, input).await {
            Ok(m) => Ok(Response::new(ProgramId { id: m.id })),
            Err(e) => Err(Status::new(tonic::Code::Aborted, e.to_string().to_owned())),
        }
    }

    #[tracing::instrument]
    async fn add_programs(
        &self,
        request: Request<ProgramList>,
    ) -> Result<Response<ProgramReply>, Status> {
        let conn = &self.db_conn;
        let input = request.into_inner().into_models();

        match ProgramMutation::create_programs(conn, input).await {
            Ok(_) => Ok(Response::new(ProgramReply { success: true })),
            Err(_) => Ok(Response::new(ProgramReply { success: false })),
        }
    }

    #[tracing::instrument]
    async fn update_program(
        &self,
        request: Request<Program>,
    ) -> Result<Response<ProgramReply>, Status> {
        let conn = &self.db_conn;
        let input = request.into_inner().into_model();

        match ProgramMutation::update_program(conn, input).await {
            Ok(_) => Ok(Response::new(ProgramReply { success: true })),
            Err(_) => Ok(Response::new(ProgramReply { success: false })),
        }
    }

    #[tracing::instrument]
    async fn delete_program(
        &self,
        request: Request<ProgramId>,
    ) -> Result<Response<ProgramReply>, Status> {
        let conn = &self.db_conn;
        let id = request.into_inner().id;

        match ProgramMutation::delete_program(conn, id).await {
            Ok(_) => Ok(Response::new(ProgramReply { success: true })),
            Err(_) => Ok(Response::new(ProgramReply { success: false })),
        }
    }
}

#[tonic::async_trait]
impl TestService for MyTestServer {
    #[tracing::instrument]
    async fn test(&self, request: Request<TestRequest>) -> Result<Response<TestReply>, Status> {
        let input = request.into_inner();

        Ok(Response::new(TestReply {
            message: format!("Hello {}!", input.name),
        }))
    }
}

pub trait ServerExt {
    fn add_grpc_service(self, db_conn: DbConn) -> Router;
}

impl ServerExt for Server {
    fn add_grpc_service(mut self, db_conn: DbConn) -> Router {
        let mut application_svc = ApplicationServiceServer::new(MyApplicationServer {
            db_conn: db_conn.clone(),
        });
        let mut program_svc = ProgramServiceServer::new(MyProgramServer {
            db_conn: db_conn.clone(),
        });
        let mut log_svc = LogServiceServer::new(MyLogServer {
            db_conn: db_conn.clone(),
        });
        let mut log_detail_svc = LogDetailServiceServer::new(MyLogDetailServer {
            db_conn: db_conn.clone(),
        });
        let mut test_svc = TestServiceServer::new(MyTestServer {});

        if CFG.server.content_gzip {
            application_svc = application_svc
                .send_compressed(CompressionEncoding::Gzip)
                .accept_compressed(CompressionEncoding::Gzip);
            program_svc = program_svc
                .send_compressed(CompressionEncoding::Gzip)
                .accept_compressed(CompressionEncoding::Gzip);
            log_svc = log_svc
                .send_compressed(CompressionEncoding::Gzip)
                .accept_compressed(CompressionEncoding::Gzip);
            log_detail_svc = log_detail_svc
                .send_compressed(CompressionEncoding::Gzip)
                .accept_compressed(CompressionEncoding::Gzip);
            test_svc = test_svc
                .send_compressed(CompressionEncoding::Gzip)
                .accept_compressed(CompressionEncoding::Gzip);
        }

        self.add_service(application_svc)
            .add_service(program_svc)
            .add_service(log_svc)
            .add_service(log_detail_svc)
            .add_service(test_svc)
    }
}
