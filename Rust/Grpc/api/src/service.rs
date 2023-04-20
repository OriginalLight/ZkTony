use grpc_core::{sea_orm::DbConn, *};

use tonic::{
    codec::CompressionEncoding,
    transport::{server::Router, server::Server, Error, Identity, ServerTlsConfig},
    Code, Request, Response, Status,
};

use super::CFG;

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

        ApplicationQuery::get_applications_in_page(conn, page, page_size)
            .await
            .map_err(|e| Status::internal(e.to_string()))
            .map(|(models, total)| {
                Response::new(ApplicationReplyPage {
                    list: models
                        .iter()
                        .map(|model| Application::from(model.clone()))
                        .collect::<Vec<Application>>(),
                    total,
                })
            })
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

        ApplicationQuery::get_by_application_id(conn, application_id)
            .await
            .map_err(|e| Status::internal(e.to_string()))
            .map(|model| {
                model
                    .map(|m| Ok(Response::new(Application::from(m))))
                    .unwrap_or_else(|| Err(Status::not_found("Cannot find application")))
            })?
    }

    #[tracing::instrument]
    async fn get_by_id(
        &self,
        request: Request<ApplicationId>,
    ) -> Result<Response<Application>, Status> {
        let conn = &self.db_conn;
        let id = request.into_inner().id;

        ApplicationQuery::get_application_by_id(conn, id)
            .await
            .map_err(|e| Status::internal(e.to_string()))
            .map(|model| {
                model
                    .map(|m| Ok(Response::new(Application::from(m))))
                    .unwrap_or_else(|| Err(Status::not_found("Cannot find application")))
            })?
    }

    #[tracing::instrument]
    async fn add_application(
        &self,
        request: Request<Application>,
    ) -> Result<Response<ApplicationId>, Status> {
        let conn = &self.db_conn;
        let input = request.into_inner().into_model();

        ApplicationMutation::create_application(conn, input)
            .await
            .map_err(|e| Status::internal(e.to_string()))
            .map(|model| Response::new(ApplicationId { id: model.id }))
    }

    #[tracing::instrument]
    async fn update_application(
        &self,
        request: Request<Application>,
    ) -> Result<Response<ApplicationReply>, Status> {
        let conn = &self.db_conn;
        let input = request.into_inner().into_model();

        ApplicationMutation::update_application(conn, input)
            .await
            .map_err(|e| Status::internal(e.to_string()))
            .map(|_| Response::new(ApplicationReply { success: true }))
    }

    #[tracing::instrument]
    async fn delete_application(
        &self,
        request: Request<ApplicationId>,
    ) -> Result<Response<ApplicationReply>, Status> {
        let conn = &self.db_conn;
        let id = request.into_inner().id;

        ApplicationMutation::delete_application(conn, id)
            .await
            .map_err(|e| Status::internal(e.to_string()))
            .map(|_| Response::new(ApplicationReply { success: true }))
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

        LogQuery::get_logs_in_page(conn, page, page_size)
            .await
            .map_err(|e| Status::internal(e.to_string()))
            .map(|(models, total)| {
                Response::new(LogReplyPage {
                    list: models
                        .iter()
                        .map(|model| Log::from(model.clone()))
                        .collect::<Vec<Log>>(),
                    total,
                })
            })
    }

    #[tracing::instrument]
    async fn get_by_id(&self, request: Request<LogId>) -> Result<Response<Log>, Status> {
        let conn = &self.db_conn;
        let id = request.into_inner().id;

        LogQuery::get_log_by_id(conn, id)
            .await
            .map_err(|e| Status::internal(e.to_string()))
            .map(|model| {
                model
                    .map(|m| Ok(Response::new(Log::from(m))))
                    .unwrap_or_else(|| Err(Status::not_found("Cannot find log")))
            })?
    }

    #[tracing::instrument]
    async fn add_log(&self, request: Request<Log>) -> Result<Response<LogId>, Status> {
        let conn = &self.db_conn;
        let input = request.into_inner().into_model();

        LogMutation::create_log(conn, input)
            .await
            .map_err(|e| Status::internal(e.to_string()))
            .map(|model| Response::new(LogId { id: model.id }))
    }

    #[tracing::instrument]
    async fn add_logs(&self, request: Request<LogList>) -> Result<Response<LogReply>, Status> {
        let conn = &self.db_conn;
        let input = request.into_inner().into_models();

        LogMutation::create_logs(conn, input)
            .await
            .map_err(|e| Status::internal(e.to_string()))
            .map(|_| Response::new(LogReply { success: true }))
    }

    #[tracing::instrument]
    async fn update_log(&self, request: Request<Log>) -> Result<Response<LogReply>, Status> {
        let conn = &self.db_conn;
        let input = request.into_inner().into_model();

        LogMutation::update_log(conn, input)
            .await
            .map_err(|e| Status::internal(e.to_string()))
            .map(|_| Response::new(LogReply { success: true }))
    }

    #[tracing::instrument]
    async fn delete_log(&self, request: Request<LogId>) -> Result<Response<LogReply>, Status> {
        let conn = &self.db_conn;
        let id = request.into_inner().id;

        LogMutation::delete_log(conn, id)
            .await
            .map_err(|e| Status::internal(e.to_string()))
            .map(|_| Response::new(LogReply { success: true }))
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

        LogDetailQuery::get_log_details_in_page(conn, page, page_size)
            .await
            .map_err(|e| Status::internal(e.to_string()))
            .map(|(models, total)| {
                Response::new(LogDetailReplyPage {
                    list: models
                        .iter()
                        .map(|model| LogDetail::from(model.clone()))
                        .collect::<Vec<LogDetail>>(),
                    total,
                })
            })
    }

    #[tracing::instrument]
    async fn get_by_id(
        &self,
        request: Request<LogDetailId>,
    ) -> Result<Response<LogDetail>, Status> {
        let conn = &self.db_conn;
        let id = request.into_inner().id;

        LogDetailQuery::get_log_detail_by_id(conn, id)
            .await
            .map_err(|e| Status::internal(e.to_string()))
            .map(|model| {
                model
                    .map(|m| Ok(Response::new(LogDetail::from(m))))
                    .unwrap_or_else(|| Err(Status::not_found("Cannot find log detail")))
            })?
    }

    #[tracing::instrument]
    async fn add_log_detail(
        &self,
        request: Request<LogDetail>,
    ) -> Result<Response<LogDetailId>, Status> {
        let conn = &self.db_conn;
        let input = request.into_inner().into_model();

        LogDetailMutation::create_log_detail(conn, input)
            .await
            .map_err(|e| Status::internal(e.to_string()))
            .map(|model| Response::new(LogDetailId { id: model.id }))
    }

    #[tracing::instrument]
    async fn add_log_details(
        &self,
        request: Request<LogDetailList>,
    ) -> Result<Response<LogDetailReply>, Status> {
        let conn = &self.db_conn;
        let input = request.into_inner().into_models();

        LogDetailMutation::create_log_details(conn, input)
            .await
            .map_err(|e| Status::internal(e.to_string()))
            .map(|_| Response::new(LogDetailReply { success: true }))
    }

    #[tracing::instrument]
    async fn update_log_detail(
        &self,
        request: Request<LogDetail>,
    ) -> Result<Response<LogDetailReply>, Status> {
        let conn = &self.db_conn;
        let input = request.into_inner().into_model();

        LogDetailMutation::update_log_detail(conn, input)
            .await
            .map_err(|e| Status::internal(e.to_string()))
            .map(|_| Response::new(LogDetailReply { success: true }))
    }

    #[tracing::instrument]
    async fn delete_log_detail(
        &self,
        request: Request<LogDetailId>,
    ) -> Result<Response<LogDetailReply>, Status> {
        let conn = &self.db_conn;
        let id = request.into_inner().id;

        LogDetailMutation::delete_log_detail(conn, id)
            .await
            .map_err(|e| Status::internal(e.to_string()))
            .map(|_| Response::new(LogDetailReply { success: true }))
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
                "Page and page size cannot be zero".to_string(),
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

        ProgramQuery::get_program_by_id(conn, id)
            .await
            .map_err(|e| Status::internal(e.to_string()))
            .map(|model| {
                model
                    .map(|m| Ok(Response::new(Program::from(m))))
                    .unwrap_or_else(|| Err(Status::not_found("Cannot find program")))
            })?
    }

    #[tracing::instrument]
    async fn add_program(&self, request: Request<Program>) -> Result<Response<ProgramId>, Status> {
        let conn = &self.db_conn;
        let input = request.into_inner().into_model();

        ProgramMutation::create_program(conn, input)
            .await
            .map_err(|e| Status::internal(e.to_string()))
            .map(|model| Response::new(ProgramId { id: model.id }))
    }

    #[tracing::instrument]
    async fn add_programs(
        &self,
        request: Request<ProgramList>,
    ) -> Result<Response<ProgramReply>, Status> {
        let conn = &self.db_conn;
        let input = request.into_inner().into_models();

        ProgramMutation::create_programs(conn, input)
            .await
            .map_err(|e| Status::internal(e.to_string()))
            .map(|_| Response::new(ProgramReply { success: true }))
    }

    #[tracing::instrument]
    async fn update_program(
        &self,
        request: Request<Program>,
    ) -> Result<Response<ProgramReply>, Status> {
        let conn = &self.db_conn;
        let input = request.into_inner().into_model();

        ProgramMutation::update_program(conn, input)
            .await
            .map_err(|e| Status::internal(e.to_string()))
            .map(|_| Response::new(ProgramReply { success: true }))
    }

    #[tracing::instrument]
    async fn delete_program(
        &self,
        request: Request<ProgramId>,
    ) -> Result<Response<ProgramReply>, Status> {
        let conn = &self.db_conn;
        let id = request.into_inner().id;

        ProgramMutation::delete_program(conn, id)
            .await
            .map_err(|e| Status::internal(e.to_string()))
            .map(|_| Response::new(ProgramReply { success: true }))
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
    fn enable_ssl(self) -> Result<Self, Error>
    where
        Self: Sized;
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

    fn enable_ssl(self) -> Result<Self, Error> {
        let cert = std::fs::read_to_string(&CFG.cert.cert).unwrap();
        let key = std::fs::read_to_string(&CFG.cert.key).unwrap();
        let identity = Identity::from_pem(cert, key);
        if CFG.server.ssl {
            self.tls_config(ServerTlsConfig::new().identity(identity.clone()))
        } else {
            Ok(self)
        }
    }
}
