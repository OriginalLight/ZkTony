use manager_core::{sea_orm::DbConn, *};

use tonic::{
    codec::CompressionEncoding,
    transport::{server::Router, server::Server, Error, Identity, ServerTlsConfig},
    Request, Response, Status,
};

use super::CFG;

use super::protobuf::{
    customer::{customer_service_server::*, *},
    instrument::{instrument_service_server::*, *},
    order::{order_service_server::*, *},
    software::{software_service_server::*, *},
};

#[derive(Debug, Default, Clone)]
pub struct MyCustomerServer {
    db_conn: DbConn,
}

#[derive(Debug, Default, Clone)]
pub struct MyInstrumentServer {
    db_conn: DbConn,
}

#[derive(Debug, Default, Clone)]
pub struct MyOrderServer {
    db_conn: DbConn,
}

#[derive(Debug, Default, Clone)]
pub struct MySoftwareServer {
    db_conn: DbConn,
}

#[tonic::async_trait]
impl CustomerService for MyCustomerServer {
    #[tracing::instrument]
    async fn get_customer_page(
        &self,
        request: Request<CustomerRequestPage>,
    ) -> Result<Response<CustomerReplyPage>, Status> {
        let conn = &self.db_conn;
        let (page, page_size) = request.into_inner().into_page();

        if page == 0 || page_size == 0 {
            return Err(Status::invalid_argument(
                "Page and page size cannot be zero",
            ));
        }

        CustomerQuery::get_customers_in_page(conn, page, page_size)
            .await
            .map_err(|e| Status::internal(e.to_string()))
            .map(|(models, total)| {
                Response::new(CustomerReplyPage {
                    list: models
                        .iter()
                        .map(|model| Customer::from(model.clone()))
                        .collect::<Vec<Customer>>(),
                    total,
                })
            })
    }

    #[tracing::instrument]
    async fn get_by_id(&self, request: Request<CustomerId>) -> Result<Response<Customer>, Status> {
        let conn = &self.db_conn;
        let id = request.into_inner().id;

        CustomerQuery::get_customer_by_id(conn, id)
            .await
            .map_err(|e| Status::internal(e.to_string()))
            .map(|model| {
                model
                    .map(|m| Ok(Response::new(Customer::from(m))))
                    .unwrap_or_else(|| Err(Status::not_found("Cannot find customer")))
            })?
    }

    #[tracing::instrument]
    async fn search_customer(
        &self,
        request: Request<CustomerSearch>,
    ) -> Result<Response<CustomerList>, Status> {
        let conn = &self.db_conn;
        let form = request.into_inner().into_form();

        CustomerQuery::search_customers(conn, form)
            .await
            .map_err(|e| Status::internal(e.to_string()))
            .map(|models| {
                Response::new(CustomerList {
                    list: models
                        .iter()
                        .map(|model| Customer::from(model.clone()))
                        .collect::<Vec<Customer>>(),
                })
            })
    }

    #[tracing::instrument]
    async fn add_customer(
        &self,
        request: Request<Customer>,
    ) -> Result<Response<CustomerId>, Status> {
        let conn = &self.db_conn;
        let customer = request.into_inner();

        CustomerMutation::create_customer(conn, customer.into_model())
            .await
            .map_err(|e| Status::internal(e.to_string()))
            .map(|model| Response::new(CustomerId { id: model.id }))
    }

    #[tracing::instrument]
    async fn update_customer(
        &self,
        request: Request<Customer>,
    ) -> Result<Response<CustomerReply>, Status> {
        let conn = &self.db_conn;
        let customer = request.into_inner();

        CustomerMutation::update_customer(conn, customer.into_model())
            .await
            .map_err(|e| Status::internal(e.to_string()))
            .map(|_| Response::new(CustomerReply { success: true }))
    }

    #[tracing::instrument]
    async fn delete_customer(
        &self,
        request: Request<CustomerId>,
    ) -> Result<Response<CustomerReply>, Status> {
        let conn = &self.db_conn;
        let id = request.into_inner().id;

        CustomerMutation::delete_customer(conn, id)
            .await
            .map_err(|e| Status::internal(e.to_string()))
            .map(|_| Response::new(CustomerReply { success: true }))
    }
}

#[tonic::async_trait]
impl InstrumentService for MyInstrumentServer {
    #[tracing::instrument]
    async fn get_instrument_page(
        &self,
        request: Request<InstrumentRequestPage>,
    ) -> Result<Response<InstrumentReplyPage>, Status> {
        let conn = &self.db_conn;
        let (page, page_size) = request.into_inner().into_page();

        if page == 0 || page_size == 0 {
            return Err(Status::invalid_argument(
                "Page and page size cannot be zero",
            ));
        }

        InstrumentQuery::get_instruments_in_page(conn, page, page_size)
            .await
            .map_err(|e| Status::internal(e.to_string()))
            .map(|(models, total)| {
                Response::new(InstrumentReplyPage {
                    list: models
                        .iter()
                        .map(|model| Instrument::from(model.clone()))
                        .collect::<Vec<Instrument>>(),
                    total,
                })
            })
    }

    #[tracing::instrument]
    async fn get_by_id(
        &self,
        request: Request<InstrumentId>,
    ) -> Result<Response<Instrument>, Status> {
        let conn = &self.db_conn;
        let id = request.into_inner().id;

        InstrumentQuery::get_instrument_by_id(conn, id)
            .await
            .map_err(|e| Status::internal(e.to_string()))
            .map(|model| {
                model
                    .map(|m| Ok(Response::new(Instrument::from(m))))
                    .unwrap_or_else(|| Err(Status::not_found("Cannot find instrument")))
            })?
    }

    #[tracing::instrument]
    async fn search_instrument(
        &self,
        request: Request<InstrumentSearch>,
    ) -> Result<Response<InstrumentList>, Status> {
        let conn = &self.db_conn;
        let form = request.into_inner().into_form();

        InstrumentQuery::search_instruments(conn, form)
            .await
            .map_err(|e| Status::internal(e.to_string()))
            .map(|models| {
                Response::new(InstrumentList {
                    list: models
                        .iter()
                        .map(|model| Instrument::from(model.clone()))
                        .collect::<Vec<Instrument>>(),
                })
            })
    }

    #[tracing::instrument]
    async fn add_instrument(
        &self,
        request: Request<Instrument>,
    ) -> Result<Response<InstrumentId>, Status> {
        let conn = &self.db_conn;
        let instrument = request.into_inner();

        InstrumentMutation::create_instrument(conn, instrument.into_model())
            .await
            .map_err(|e| Status::internal(e.to_string()))
            .map(|model| Response::new(InstrumentId { id: model.id }))
    }

    #[tracing::instrument]
    async fn update_instrument(
        &self,
        request: Request<Instrument>,
    ) -> Result<Response<InstrumentReply>, Status> {
        let conn = &self.db_conn;
        let instrument = request.into_inner();

        InstrumentMutation::update_instrument(conn, instrument.into_model())
            .await
            .map_err(|e| Status::internal(e.to_string()))
            .map(|_| Response::new(InstrumentReply { success: true }))
    }

    #[tracing::instrument]
    async fn delete_instrument(
        &self,
        request: Request<InstrumentId>,
    ) -> Result<Response<InstrumentReply>, Status> {
        let conn = &self.db_conn;
        let id = request.into_inner().id;

        InstrumentMutation::delete_instrument(conn, id)
            .await
            .map_err(|e| Status::internal(e.to_string()))
            .map(|_| Response::new(InstrumentReply { success: true }))
    }
}

#[tonic::async_trait]
impl OrderService for MyOrderServer {
    #[tracing::instrument]
    async fn get_order_page(
        &self,
        request: Request<OrderRequestPage>,
    ) -> Result<Response<OrderReplyPage>, Status> {
        let conn = &self.db_conn;
        let (page, page_size) = request.into_inner().into_page();

        if page == 0 || page_size == 0 {
            return Err(Status::invalid_argument(
                "Page and page size cannot be zero",
            ));
        }

        OrderQuery::get_orders_in_page(conn, page, page_size)
            .await
            .map_err(|e| Status::internal(e.to_string()))
            .map(|(models, total)| {
                Response::new(OrderReplyPage {
                    list: models
                        .iter()
                        .map(|model| Order::from(model.clone()))
                        .collect::<Vec<Order>>(),
                    total,
                })
            })
    }

    #[tracing::instrument]
    async fn get_by_id(&self, request: Request<OrderId>) -> Result<Response<Order>, Status> {
        let conn = &self.db_conn;
        let id = request.into_inner().id;

        OrderQuery::get_order_by_id(conn, id)
            .await
            .map_err(|e| Status::internal(e.to_string()))
            .map(|model| {
                model
                    .map(|m| Ok(Response::new(Order::from(m))))
                    .unwrap_or_else(|| Err(Status::not_found("Cannot find order")))
            })?
    }

    #[tracing::instrument]
    async fn search_order(
        &self,
        request: Request<OrderSearch>,
    ) -> Result<Response<OrderList>, Status> {
        let conn = &self.db_conn;
        let form = request.into_inner().into_form();

        OrderQuery::search_orders(conn, form)
            .await
            .map_err(|e| Status::internal(e.to_string()))
            .map(|models| {
                Response::new(OrderList {
                    list: models
                        .iter()
                        .map(|model| Order::from(model.clone()))
                        .collect::<Vec<Order>>(),
                })
            })
    }

    #[tracing::instrument]
    async fn add_order(&self, request: Request<Order>) -> Result<Response<OrderId>, Status> {
        let conn = &self.db_conn;
        let order = request.into_inner().into_model();

        OrderMutation::create_order(conn, order)
            .await
            .map_err(|e| Status::internal(e.to_string()))
            .map(|model| Response::new(OrderId { id: model.id }))
    }

    #[tracing::instrument]
    async fn update_order(&self, request: Request<Order>) -> Result<Response<OrderReply>, Status> {
        let conn = &self.db_conn;
        let order = request.into_inner().into_model();

        OrderMutation::update_order(conn, order)
            .await
            .map_err(|e| Status::internal(e.to_string()))
            .map(|_| Response::new(OrderReply { success: true }))
    }

    #[tracing::instrument]
    async fn delete_order(
        &self,
        request: Request<OrderId>,
    ) -> Result<Response<OrderReply>, Status> {
        let conn = &self.db_conn;
        let id = request.into_inner().id;

        OrderMutation::delete_order(conn, id)
            .await
            .map_err(|e| Status::internal(e.to_string()))
            .map(|_| Response::new(OrderReply { success: true }))
    }
}

#[tonic::async_trait]
impl SoftwareService for MySoftwareServer {
    #[tracing::instrument]
    async fn get_software_page(
        &self,
        request: Request<SoftwareRequestPage>,
    ) -> Result<Response<SoftwareReplyPage>, Status> {
        let conn = &self.db_conn;
        let (page, page_size) = request.into_inner().into_page();

        if page == 0 || page_size == 0 {
            return Err(Status::invalid_argument(
                "Page and page size cannot be zero",
            ));
        }

        SoftwareQuery::get_software_in_page(conn, page, page_size)
            .await
            .map_err(|e| Status::internal(e.to_string()))
            .map(|(models, total)| {
                Response::new(SoftwareReplyPage {
                    list: models
                        .iter()
                        .map(|model| Software::from(model.clone()))
                        .collect::<Vec<Software>>(),
                    total,
                })
            })
    }

    #[tracing::instrument]
    async fn get_by_id(&self, request: Request<SoftwareId>) -> Result<Response<Software>, Status> {
        let conn = &self.db_conn;
        let id = request.into_inner().id;

        SoftwareQuery::get_software_by_id(conn, id)
            .await
            .map_err(|e| Status::internal(e.to_string()))
            .map(|model| {
                model
                    .map(|m| Ok(Response::new(Software::from(m))))
                    .unwrap_or_else(|| Err(Status::not_found("Cannot find software")))
            })?
    }

    #[tracing::instrument]
    async fn search_software(
        &self,
        request: Request<SoftwareSearch>,
    ) -> Result<Response<SoftwareList>, Status> {
        let conn = &self.db_conn;
        let form = request.into_inner().into_form();

        SoftwareQuery::search_software(conn, form)
            .await
            .map_err(|e| Status::internal(e.to_string()))
            .map(|models| {
                Response::new(SoftwareList {
                    list: models
                        .iter()
                        .map(|model| Software::from(model.clone()))
                        .collect::<Vec<Software>>(),
                })
            })
    }

    #[tracing::instrument]
    async fn add_software(
        &self,
        request: Request<Software>,
    ) -> Result<Response<SoftwareId>, Status> {
        let conn = &self.db_conn;
        let software = request.into_inner().into_model();

        SoftwareMutation::create_software(conn, software)
            .await
            .map_err(|e| Status::internal(e.to_string()))
            .map(|model| Response::new(SoftwareId { id: model.id }))
    }

    #[tracing::instrument]
    async fn update_software(
        &self,
        request: Request<Software>,
    ) -> Result<Response<SoftwareReply>, Status> {
        let conn = &self.db_conn;
        let software = request.into_inner().into_model();

        SoftwareMutation::update_software(conn, software)
            .await
            .map_err(|e| Status::internal(e.to_string()))
            .map(|_| Response::new(SoftwareReply { success: true }))
    }

    #[tracing::instrument]
    async fn delete_software(
        &self,
        request: Request<SoftwareId>,
    ) -> Result<Response<SoftwareReply>, Status> {
        let conn = &self.db_conn;
        let id = request.into_inner().id;

        SoftwareMutation::delete_software(conn, id)
            .await
            .map_err(|e| Status::internal(e.to_string()))
            .map(|_| Response::new(SoftwareReply { success: true }))
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
        let mut customer_svc = CustomerServiceServer::new(MyCustomerServer {
            db_conn: db_conn.clone(),
        });
        let mut instrument_svc = InstrumentServiceServer::new(MyInstrumentServer {
            db_conn: db_conn.clone(),
        });
        let mut order_svc = OrderServiceServer::new(MyOrderServer {
            db_conn: db_conn.clone(),
        });
        let mut software_svc = SoftwareServiceServer::new(MySoftwareServer {
            db_conn: db_conn.clone(),
        });

        if CFG.server.content_gzip {
            customer_svc = customer_svc
                .send_compressed(CompressionEncoding::Gzip)
                .accept_compressed(CompressionEncoding::Gzip);
            instrument_svc = instrument_svc
                .send_compressed(CompressionEncoding::Gzip)
                .accept_compressed(CompressionEncoding::Gzip);
            order_svc = order_svc
                .send_compressed(CompressionEncoding::Gzip)
                .accept_compressed(CompressionEncoding::Gzip);
            software_svc = software_svc
                .send_compressed(CompressionEncoding::Gzip)
                .accept_compressed(CompressionEncoding::Gzip);
        }

        self.add_service(customer_svc)
            .add_service(instrument_svc)
            .add_service(order_svc)
            .add_service(software_svc)
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
