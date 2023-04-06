use tonic_health::pb::health_server::{Health, HealthServer};

use crate::protobuf::{
    application::application_service_server::ApplicationServiceServer,
    log::log_service_server::LogServiceServer,
    log_detail::log_detail_service_server::LogDetailServiceServer,
    program::program_service_server::ProgramServiceServer,
    test::test_service_server::TestServiceServer,
};

use super::service::{
    MyApplicationServer, MyLogDetailServer, MyLogServer, MyProgramServer, MyTestServer,
};

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

    health_reporter
        .set_serving::<TestServiceServer<MyTestServer>>()
        .await;

    health_service
}
