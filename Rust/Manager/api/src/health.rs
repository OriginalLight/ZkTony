use tonic_health::pb::health_server::{Health, HealthServer};

use crate::protobuf::{
    customer::customer_service_server::CustomerServiceServer,
    instrument::instrument_service_server::InstrumentServiceServer,
    order::order_service_server::OrderServiceServer,
    software::software_service_server::SoftwareServiceServer,
};

use super::service::{MyCustomerServer, MyInstrumentServer, MyOrderServer, MySoftwareServer};

pub async fn health_svc() -> HealthServer<impl Health> {
    let (mut health_reporter, health_service) = tonic_health::server::health_reporter();
    health_reporter
        .set_serving::<CustomerServiceServer<MyCustomerServer>>()
        .await;

    health_reporter
        .set_serving::<InstrumentServiceServer<MyInstrumentServer>>()
        .await;

    health_reporter
        .set_serving::<OrderServiceServer<MyOrderServer>>()
        .await;

    health_reporter
        .set_serving::<SoftwareServiceServer<MySoftwareServer>>()
        .await;

    health_service
}
