use tonic_health::proto::health_server::{Health, HealthServer};

use crate::protobuf::{
    application::application_service_server::ApplicationServiceServer,
    log::log_service_server::LogServiceServer,
    log_detail::log_detail_service_server::LogDetailServiceServer,
    program::program_service_server::ProgramServiceServer,
};

use super::service::{MyApplicationServer, MyLogDetailServer, MyLogServer, MyProgramServer};

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
