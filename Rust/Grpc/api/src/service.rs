use grpc_core::{sea_orm::DbConn, *};

use tonic::{
    codec::CompressionEncoding,
    transport::{server::Router, server::Server, Identity, ServerTlsConfig},
    Code, Request, Response, Status,
};

use super::{
    health::health_svc,
    protobuf::{
        application::{application_service_server::*, *},
        log::{log_service_server::*, *},
        log_detail::{log_detail_service_server::*, *},
        program::{program_service_server::*, *},
        test::{test_service_server::*, *},
    },
    CFG,
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
    async fn query(
        &self,
        request: Request<ApplicationRequestQuery>,
    ) -> Result<Response<ApplicationReplyQuery>, Status> {
        let conn = &self.db_conn;
        let form = request.into_inner().into_query();

        if form.page == 0 || form.page_size == 0 {
            return Err(Status::invalid_argument(
                "Page and page size cannot be zero",
            ));
        }

        ApplicationQuery::query(conn, form)
            .await
            .map_err(|e| Status::internal(e.to_string()))
            .map(|(models, total)| {
                Response::new(ApplicationReplyQuery {
                    list: models
                        .iter()
                        .map(|model| Application::from(model.clone()))
                        .collect::<Vec<Application>>(),
                    total,
                })
            })
    }

    #[tracing::instrument]
    async fn insert(
        &self,
        request: Request<Application>,
    ) -> Result<Response<ApplicationReply>, Status> {
        let conn = &self.db_conn;
        let model = request.into_inner().into_model();

        ApplicationMutation::insert(conn, model)
            .await
            .map_err(|e| Status::internal(e.to_string()))
            .map(|_| Response::new(ApplicationReply { success: true }))
    }

    #[tracing::instrument]
    async fn insert_batch(
        &self,
        request: Request<ApplicationList>,
    ) -> Result<Response<ApplicationReply>, Status> {
        let conn = &self.db_conn;
        let models = request.into_inner().into_models();

        ApplicationMutation::insert_batch(conn, models)
            .await
            .map_err(|e| Status::internal(e.to_string()))
            .map(|_| Response::new(ApplicationReply { success: true }))
    }

    #[tracing::instrument]
    async fn update(
        &self,
        request: Request<Application>,
    ) -> Result<Response<ApplicationReply>, Status> {
        let conn = &self.db_conn;
        let model = request.into_inner().into_model();

        ApplicationMutation::update(conn, model)
            .await
            .map_err(|e| Status::internal(e.to_string()))
            .map(|_| Response::new(ApplicationReply { success: true }))
    }

    #[tracing::instrument]
    async fn update_batch(
        &self,
        request: Request<ApplicationList>,
    ) -> Result<Response<ApplicationReply>, Status> {
        let conn = &self.db_conn;
        let models = request.into_inner().into_models();

        ApplicationMutation::update_batch(conn, models)
            .await
            .map_err(|e| Status::internal(e.to_string()))
            .map(|_| Response::new(ApplicationReply { success: true }))
    }

    #[tracing::instrument]
    async fn delete(
        &self,
        request: Request<Application>,
    ) -> Result<Response<ApplicationReply>, Status> {
        let conn = &self.db_conn;
        let model = request.into_inner().into_model();

        ApplicationMutation::delete(conn, model)
            .await
            .map_err(|e| Status::internal(e.to_string()))
            .map(|_| Response::new(ApplicationReply { success: true }))
    }

    #[tracing::instrument]
    async fn delete_batch(
        &self,
        request: Request<ApplicationList>,
    ) -> Result<Response<ApplicationReply>, Status> {
        let conn = &self.db_conn;
        let models = request.into_inner().into_models();

        ApplicationMutation::delete_batch(conn, models)
            .await
            .map_err(|e| Status::internal(e.to_string()))
            .map(|_| Response::new(ApplicationReply { success: true }))
    }
}

#[tonic::async_trait]
impl LogService for MyLogServer {
    #[tracing::instrument]
    async fn query(
        &self,
        request: Request<LogRequestQuery>,
    ) -> Result<Response<LogReplyQuery>, Status> {
        let conn = &self.db_conn;
        let form = request.into_inner().into_query();

        if form.page == 0 || form.page_size == 0 {
            return Err(Status::invalid_argument(
                "Page and page size cannot be zero",
            ));
        }

        LogQuery::query(conn, form)
            .await
            .map_err(|e| Status::internal(e.to_string()))
            .map(|(models, total)| {
                Response::new(LogReplyQuery {
                    list: models
                        .iter()
                        .map(|model| Log::from(model.clone()))
                        .collect::<Vec<Log>>(),
                    total,
                })
            })
    }

    #[tracing::instrument]
    async fn insert(&self, request: Request<Log>) -> Result<Response<LogReply>, Status> {
        let conn = &self.db_conn;
        let model = request.into_inner().into_model();

        LogMutation::insert(conn, model)
            .await
            .map_err(|e| Status::internal(e.to_string()))
            .map(|_| Response::new(LogReply { success: true }))
    }

    #[tracing::instrument]
    async fn insert_batch(&self, request: Request<LogList>) -> Result<Response<LogReply>, Status> {
        let conn = &self.db_conn;
        let models = request.into_inner().into_models();

        LogMutation::insert_batch(conn, models)
            .await
            .map_err(|e| Status::internal(e.to_string()))
            .map(|_| Response::new(LogReply { success: true }))
    }

    #[tracing::instrument]
    async fn update(&self, request: Request<Log>) -> Result<Response<LogReply>, Status> {
        let conn = &self.db_conn;
        let model = request.into_inner().into_model();

        LogMutation::update(conn, model)
            .await
            .map_err(|e| Status::internal(e.to_string()))
            .map(|_| Response::new(LogReply { success: true }))
    }

    #[tracing::instrument]
    async fn update_batch(&self, request: Request<LogList>) -> Result<Response<LogReply>, Status> {
        let conn = &self.db_conn;
        let models = request.into_inner().into_models();

        LogMutation::update_batch(conn, models)
            .await
            .map_err(|e| Status::internal(e.to_string()))
            .map(|_| Response::new(LogReply { success: true }))
    }

    #[tracing::instrument]
    async fn delete(&self, request: Request<Log>) -> Result<Response<LogReply>, Status> {
        let conn = &self.db_conn;
        let model = request.into_inner().into_model();

        LogMutation::delete(conn, model)
            .await
            .map_err(|e| Status::internal(e.to_string()))
            .map(|_| Response::new(LogReply { success: true }))
    }

    #[tracing::instrument]
    async fn delete_batch(&self, request: Request<LogList>) -> Result<Response<LogReply>, Status> {
        let conn = &self.db_conn;
        let models = request.into_inner().into_models();

        LogMutation::delete_batch(conn, models)
            .await
            .map_err(|e| Status::internal(e.to_string()))
            .map(|_| Response::new(LogReply { success: true }))
    }
}

#[tonic::async_trait]
impl LogDetailService for MyLogDetailServer {
    #[tracing::instrument]
    async fn query(
        &self,
        request: Request<LogDetailRequestQuery>,
    ) -> Result<Response<LogDetailReplyQuery>, Status> {
        let conn = &self.db_conn;
        let form = request.into_inner().into_query();

        if form.page == 0 || form.page_size == 0 {
            return Err(Status::invalid_argument(
                "Page and page size cannot be zero",
            ));
        }

        LogDetailQuery::query(conn, form)
            .await
            .map_err(|e| Status::internal(e.to_string()))
            .map(|(models, total)| {
                Response::new(LogDetailReplyQuery {
                    list: models
                        .iter()
                        .map(|model| LogDetail::from(model.clone()))
                        .collect::<Vec<LogDetail>>(),
                    total,
                })
            })
    }

    #[tracing::instrument]
    async fn insert(
        &self,
        request: Request<LogDetail>,
    ) -> Result<Response<LogDetailReply>, Status> {
        let conn = &self.db_conn;
        let model = request.into_inner().into_model();

        LogDetailMutation::insert(conn, model)
            .await
            .map_err(|e| Status::internal(e.to_string()))
            .map(|_| Response::new(LogDetailReply { success: true }))
    }

    #[tracing::instrument]
    async fn insert_batch(
        &self,
        request: Request<LogDetailList>,
    ) -> Result<Response<LogDetailReply>, Status> {
        let conn = &self.db_conn;
        let models = request.into_inner().into_models();

        LogDetailMutation::insert_batch(conn, models)
            .await
            .map_err(|e| Status::internal(e.to_string()))
            .map(|_| Response::new(LogDetailReply { success: true }))
    }

    #[tracing::instrument]
    async fn update(
        &self,
        request: Request<LogDetail>,
    ) -> Result<Response<LogDetailReply>, Status> {
        let conn = &self.db_conn;
        let model = request.into_inner().into_model();

        LogDetailMutation::update(conn, model)
            .await
            .map_err(|e| Status::internal(e.to_string()))
            .map(|_| Response::new(LogDetailReply { success: true }))
    }

    #[tracing::instrument]
    async fn update_batch(
        &self,
        request: Request<LogDetailList>,
    ) -> Result<Response<LogDetailReply>, Status> {
        let conn = &self.db_conn;
        let models = request.into_inner().into_models();

        LogDetailMutation::update_batch(conn, models)
            .await
            .map_err(|e| Status::internal(e.to_string()))
            .map(|_| Response::new(LogDetailReply { success: true }))
    }

    #[tracing::instrument]
    async fn delete(
        &self,
        request: Request<LogDetail>,
    ) -> Result<Response<LogDetailReply>, Status> {
        let conn = &self.db_conn;
        let model = request.into_inner().into_model();

        LogDetailMutation::delete(conn, model)
            .await
            .map_err(|e| Status::internal(e.to_string()))
            .map(|_| Response::new(LogDetailReply { success: true }))
    }

    #[tracing::instrument]
    async fn delete_batch(
        &self,
        request: Request<LogDetailList>,
    ) -> Result<Response<LogDetailReply>, Status> {
        let conn = &self.db_conn;
        let models = request.into_inner().into_models();

        LogDetailMutation::delete_batch(conn, models)
            .await
            .map_err(|e| Status::internal(e.to_string()))
            .map(|_| Response::new(LogDetailReply { success: true }))
    }
}

#[tonic::async_trait]
impl ProgramService for MyProgramServer {
    #[tracing::instrument]
    async fn query(
        &self,
        request: Request<ProgramRequestQuery>,
    ) -> Result<Response<ProgramReplyQuery>, Status> {
        let conn = &self.db_conn;
        let form = request.into_inner().into_query();

        if form.page == 0 || form.page_size == 0 {
            return Err(Status::new(
                Code::InvalidArgument,
                "Page and page size cannot be zero".to_string(),
            ));
        }

        match ProgramQuery::query(conn, form).await {
            Ok((models, total)) => Ok(Response::new(ProgramReplyQuery {
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
    async fn insert(&self, request: Request<Program>) -> Result<Response<ProgramReply>, Status> {
        let conn = &self.db_conn;
        let model = request.into_inner().into_model();

        ProgramMutation::insert(conn, model)
            .await
            .map_err(|e| Status::internal(e.to_string()))
            .map(|_| Response::new(ProgramReply { success: true }))
    }

    #[tracing::instrument]
    async fn insert_batch(
        &self,
        request: Request<ProgramList>,
    ) -> Result<Response<ProgramReply>, Status> {
        let conn = &self.db_conn;
        let models = request.into_inner().into_models();

        ProgramMutation::insert_batch(conn, models)
            .await
            .map_err(|e| Status::internal(e.to_string()))
            .map(|_| Response::new(ProgramReply { success: true }))
    }

    #[tracing::instrument]
    async fn update(&self, request: Request<Program>) -> Result<Response<ProgramReply>, Status> {
        let conn = &self.db_conn;
        let model = request.into_inner().into_model();

        ProgramMutation::update(conn, model)
            .await
            .map_err(|e| Status::internal(e.to_string()))
            .map(|_| Response::new(ProgramReply { success: true }))
    }

    #[tracing::instrument]
    async fn update_batch(
        &self,
        request: Request<ProgramList>,
    ) -> Result<Response<ProgramReply>, Status> {
        let conn = &self.db_conn;
        let models = request.into_inner().into_models();

        ProgramMutation::update_batch(conn, models)
            .await
            .map_err(|e| Status::internal(e.to_string()))
            .map(|_| Response::new(ProgramReply { success: true }))
    }

    #[tracing::instrument]
    async fn delete(&self, request: Request<Program>) -> Result<Response<ProgramReply>, Status> {
        let conn = &self.db_conn;
        let model = request.into_inner().into_model();

        ProgramMutation::delete(conn, model)
            .await
            .map_err(|e| Status::internal(e.to_string()))
            .map(|_| Response::new(ProgramReply { success: true }))
    }

    #[tracing::instrument]
    async fn delete_batch(
        &self,
        request: Request<ProgramList>,
    ) -> Result<Response<ProgramReply>, Status> {
        let conn = &self.db_conn;
        let models = request.into_inner().into_models();

        ProgramMutation::delete_batch(conn, models)
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

#[tonic::async_trait]
pub trait ServerExt {
    async fn add_grpc_service(self, db_conn: DbConn) -> Router;
    fn enable_ssl(self) -> Self
    where
        Self: Sized;
}

#[tonic::async_trait]
impl ServerExt for Server {
    async fn add_grpc_service(mut self, db_conn: DbConn) -> Router {
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
            .add_service(health_svc().await)
    }

    fn enable_ssl(self) -> Self {
        let cert = std::fs::read_to_string(&CFG.cert.cert).unwrap();
        let key = std::fs::read_to_string(&CFG.cert.key).unwrap();
        let identity = Identity::from_pem(cert, key);
        if CFG.server.ssl {
            self.tls_config(ServerTlsConfig::new().identity(identity.clone()))
                .unwrap()
        } else {
            self
        }
    }
}
