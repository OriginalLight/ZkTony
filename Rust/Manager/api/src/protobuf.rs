use chrono::NaiveDateTime;

use entity::{form::*, CustomerModel, InstrumentModel, OrderModel, SoftwareModel};

use customer::*;
use instrument::*;
use order::*;
use software::*;

pub mod customer {
    tonic::include_proto!("customer");
}

pub mod instrument {
    tonic::include_proto!("instrument");
}

pub mod order {
    tonic::include_proto!("order");
}

pub mod software {
    tonic::include_proto!("software");
}

impl Customer {
    pub fn into_model(self) -> CustomerModel {
        CustomerModel {
            id: self.id,
            name: self.name,
            phone: self.phone,
            address: self.address,
            industry: self.industry,
            source: self.source,
            remarks: Some(self.remarks),
            create_by: Some(self.create_by),
            create_time: self.create_time.to_naivedatetime(),
        }
    }
}

impl CustomerList {
    pub fn into_models(self) -> Vec<CustomerModel> {
        self.list
            .into_iter()
            .map(|customer| customer.into_model())
            .collect()
    }
}

impl CustomerRequestPage {
    pub fn into_page(self) -> (u64, u64) {
        (self.page, self.page_size)
    }
}

impl CustomerSearch {
    pub fn into_form(self) -> CustomerSearchForm {
        CustomerSearchForm {
            id: self.id,
            name: self.name,
            phone: self.phone,
            address: self.address,
            begin_time: self.begin_time,
            end_time: self.end_time,
        }
    }
}

impl From<CustomerModel> for Customer {
    fn from(model: CustomerModel) -> Self {
        Customer {
            id: model.id,
            name: model.name,
            phone: model.phone,
            address: model.address,
            industry: model.industry,
            source: model.source,
            remarks: model.remarks.unwrap_or_default(),
            create_by: model.create_by.unwrap_or_default(),
            create_time: model.create_time.to_string(),
        }
    }
}

impl Instrument {
    pub fn into_model(self) -> InstrumentModel {
        InstrumentModel {
            id: self.id,
            name: self.name,
            model: self.model,
            power: self.power,
            frequency: self.frequency,
            voltage: self.voltage,
            attachment: self.attachment,
            remarks: Some(self.remarks),
            create_by: Some(self.create_by),
            create_time: self.create_time.to_naivedatetime(),
        }
    }
}

impl InstrumentList {
    pub fn into_models(self) -> Vec<InstrumentModel> {
        self.list
            .into_iter()
            .map(|instrument| instrument.into_model())
            .collect()
    }
}

impl InstrumentRequestPage {
    pub fn into_page(self) -> (u64, u64) {
        (self.page, self.page_size)
    }
}

impl InstrumentSearch {
    pub fn into_form(self) -> InstrumentSearchForm {
        InstrumentSearchForm {
            id: self.id,
            name: self.name,
            model: self.model,
            begin_time: self.begin_time,
            end_time: self.end_time,
        }
    }
}

impl From<InstrumentModel> for Instrument {
    fn from(model: InstrumentModel) -> Self {
        Instrument {
            id: model.id,
            name: model.name,
            model: model.model,
            power: model.power,
            frequency: model.frequency,
            voltage: model.voltage,
            attachment: model.attachment,
            remarks: model.remarks.unwrap_or_default(),
            create_by: model.create_by.unwrap_or_default(),
            create_time: model.create_time.to_string(),
        }
    }
}

impl Order {
    pub fn into_model(self) -> OrderModel {
        OrderModel {
            id: self.id,
            customer_id: self.customer_id,
            instrument_id: self.instrument_id,
            software_id: self.software_id,
            express_company: self.express_company,
            express_number: self.express_number,
            instrument_number: self.instrument_number,
            instrument_time: self.instrument_time.to_naivedatetime(),
            attachment: self.attachment,
            remarks: Some(self.remarks),
            create_by: Some(self.create_by),
            create_time: self.create_time.to_naivedatetime(),
        }
    }
}

impl OrderList {
    pub fn into_models(self) -> Vec<OrderModel> {
        self.list
            .into_iter()
            .map(|order| order.into_model())
            .collect()
    }
}

impl OrderRequestPage {
    pub fn into_page(self) -> (u64, u64) {
        (self.page, self.page_size)
    }
}

impl OrderSearch {
    pub fn into_form(self) -> OrderSearchForm {
        OrderSearchForm {
            id: self.id,
            customer_id: self.customer_id,
            instrument_id: self.instrument_id,
            software_id: self.software_id,
            express_number: self.express_number,
            instrument_number: self.instrument_number,
            begin_time: self.begin_time,
            end_time: self.end_time,
        }
    }
}

impl From<OrderModel> for Order {
    fn from(model: OrderModel) -> Self {
        Order {
            id: model.id,
            customer_id: model.customer_id,
            instrument_id: model.instrument_id,
            software_id: model.software_id,
            express_company: model.express_company,
            express_number: model.express_number,
            instrument_number: model.instrument_number,
            instrument_time: model.instrument_time.to_string(),
            attachment: model.attachment,
            remarks: model.remarks.unwrap_or_default(),
            create_by: model.create_by.unwrap_or_default(),
            create_time: model.create_time.to_string(),
        }
    }
}

impl Software {
    pub fn into_model(self) -> SoftwareModel {
        SoftwareModel {
            id: self.id,
            package: self.package,
            version_name: self.version_name,
            version_code: self.version_code,
            build_type: self.build_type,
            remarks: Some(self.remarks),
            create_by: Some(self.create_by),
            create_time: self.create_time.to_naivedatetime(),
        }
    }
}

impl SoftwareList {
    pub fn into_models(self) -> Vec<SoftwareModel> {
        self.list
            .into_iter()
            .map(|software| software.into_model())
            .collect()
    }
}

impl SoftwareRequestPage {
    pub fn into_page(self) -> (u64, u64) {
        (self.page, self.page_size)
    }
}

impl SoftwareSearch {
    pub fn into_form(self) -> SoftwareSearchForm {
        SoftwareSearchForm {
            id: self.id,
            package: self.package,
        }
    }
}

impl From<SoftwareModel> for Software {
    fn from(model: SoftwareModel) -> Self {
        Software {
            id: model.id,
            package: model.package,
            version_name: model.version_name,
            version_code: model.version_code,
            build_type: model.build_type,
            remarks: model.remarks.unwrap_or_default(),
            create_by: model.create_by.unwrap_or_default(),
            create_time: model.create_time.to_string(),
        }
    }
}
// String -> Option<NaiveDateTime>
pub trait StringExt {
    fn to_naivedatetime(&self) -> Option<NaiveDateTime>;
}

impl StringExt for String {
    fn to_naivedatetime(&self) -> Option<NaiveDateTime> {
        NaiveDateTime::parse_from_str(self, "%Y-%m-%d %H:%M:%S").ok()
    }
}

// Option<NaiveDateTime> -> String
pub trait NaiveDateTimeExt {
    fn to_string(&self) -> String;
}

impl NaiveDateTimeExt for Option<NaiveDateTime> {
    fn to_string(&self) -> String {
        match self {
            Some(time) => time.format("%Y-%m-%d %H:%M:%S").to_string(),
            None => "".to_string(),
        }
    }
}
