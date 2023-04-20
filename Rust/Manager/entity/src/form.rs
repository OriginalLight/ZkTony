#[derive(Clone, Debug)]
pub struct CustomerSearchForm {
    pub id: String,
    pub name: String,
    pub phone: String,
    pub address: String,
    pub begin_time: String,
    pub end_time: String,
}

#[derive(Clone, Debug)]
pub struct InstrumentSearchForm {
    pub id: String,
    pub name: String,
    pub model: String,
    pub begin_time: String,
    pub end_time: String,
}

#[derive(Clone, Debug)]
pub struct OrderSearchForm {
    pub id: String,
    pub customer_id: String,
    pub instrument_id: String,
    pub software_id: String,
    pub express_number: String,
    pub instrument_number: String,
    pub begin_time: String,
    pub end_time: String,
}

#[derive(Clone, Debug)]
pub struct SoftwareSearchForm {
    pub id: String,
    pub package: String,
}
