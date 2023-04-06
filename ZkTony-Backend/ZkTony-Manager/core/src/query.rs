use ::entity::{form::*, *};
use chrono::NaiveDateTime;
use sea_orm::*;

pub struct CustomerQuery;

pub struct InstrumentQuery;

pub struct OrderQuery;

pub struct SoftwareQuery;

impl CustomerQuery {
    pub async fn get_customer_by_id(
        db: &DbConn,
        id: String,
    ) -> Result<Option<CustomerModel>, DbErr> {
        CustomerEntity::find_by_id(id).one(db).await
    }

    pub async fn get_customers_in_page(
        db: &DbConn,
        page: u64,
        page_size: u64,
    ) -> Result<(Vec<CustomerModel>, u64), DbErr> {
        let paginator = CustomerEntity::find()
            .order_by_asc(CustomerColumn::Id)
            .paginate(db, page_size);
        let num_pages = paginator.num_pages().await?;

        paginator.fetch_page(page - 1).await.map(|p| (p, num_pages))
    }

    pub async fn search_customers(
        db: &DbConn,
        form: CustomerSearchForm,
    ) -> Result<Vec<CustomerModel>, DbErr> {
        let mut query = CustomerEntity::find();

        if !form.id.is_empty() {
            query = query.filter(CustomerColumn::Id.eq(form.id));
        }

        if !form.name.is_empty() {
            query = query.filter(CustomerColumn::Name.like(&format!("%{}%", form.name)));
        }

        if !form.address.is_empty() {
            query = query.filter(CustomerColumn::Address.like(&format!("%{}%", form.address)));
        }

        if !form.phone.is_empty() {
            query = query.filter(CustomerColumn::Phone.eq(form.phone));
        }

        if !form.begin_time.is_empty() {
            let begin_time = form.begin_time + " 00:00:00";
            let t = NaiveDateTime::parse_from_str(&begin_time, "%Y-%m-%d %H:%M:%S").unwrap();
            query = query.filter(CustomerColumn::CreateTime.gte(t));
        }

        if !form.end_time.is_empty() {
            let end_time = form.end_time + " 23:59:59";
            let t = NaiveDateTime::parse_from_str(&end_time, "%Y-%m-%d %H:%M:%S").unwrap();
            query = query.filter(CustomerColumn::CreateTime.lte(t));
        }

        query = query.order_by_desc(CustomerColumn::Id);

        query.all(db).await
    }
}

impl InstrumentQuery {
    pub async fn get_instrument_by_id(
        db: &DbConn,
        id: String,
    ) -> Result<Option<InstrumentModel>, DbErr> {
        InstrumentEntity::find_by_id(id).one(db).await
    }

    pub async fn get_instruments_in_page(
        db: &DbConn,
        page: u64,
        page_size: u64,
    ) -> Result<(Vec<InstrumentModel>, u64), DbErr> {
        let paginator = InstrumentEntity::find()
            .order_by_asc(InstrumentColumn::Id)
            .paginate(db, page_size);
        let num_pages = paginator.num_pages().await?;

        paginator.fetch_page(page - 1).await.map(|p| (p, num_pages))
    }

    pub async fn search_instruments(
        db: &DbConn,
        form: InstrumentSearchForm,
    ) -> Result<Vec<InstrumentModel>, DbErr> {
        let mut query = InstrumentEntity::find();

        if !form.id.is_empty() {
            query = query.filter(InstrumentColumn::Id.eq(form.id));
        }

        if !form.name.is_empty() {
            query = query.filter(InstrumentColumn::Name.like(&format!("%{}%", form.name)));
        }

        if !form.model.is_empty() {
            query = query.filter(InstrumentColumn::Model.like(&format!("%{}%", form.model)));
        }

        if !form.begin_time.is_empty() {
            let begin_time = form.begin_time + " 00:00:00";
            let t = NaiveDateTime::parse_from_str(&begin_time, "%Y-%m-%d %H:%M:%S").unwrap();
            query = query.filter(InstrumentColumn::CreateTime.gte(t));
        }

        if !form.end_time.is_empty() {
            let end_time = form.end_time + " 23:59:59";
            let t = NaiveDateTime::parse_from_str(&end_time, "%Y-%m-%d %H:%M:%S").unwrap();
            query = query.filter(InstrumentColumn::CreateTime.lte(t));
        }

        query = query.order_by_desc(InstrumentColumn::Id);

        query.all(db).await
    }
}

impl OrderQuery {
    pub async fn get_order_by_id(db: &DbConn, id: String) -> Result<Option<OrderModel>, DbErr> {
        OrderEntity::find_by_id(id).one(db).await
    }

    pub async fn get_orders_in_page(
        db: &DbConn,
        page: u64,
        page_size: u64,
    ) -> Result<(Vec<OrderModel>, u64), DbErr> {
        let paginator = OrderEntity::find()
            .order_by_asc(OrderColumn::Id)
            .paginate(db, page_size);
        let num_pages = paginator.num_pages().await?;

        paginator.fetch_page(page - 1).await.map(|p| (p, num_pages))
    }

    pub async fn search_orders(
        db: &DbConn,
        form: OrderSearchForm,
    ) -> Result<Vec<OrderModel>, DbErr> {
        let mut query = OrderEntity::find();

        if !form.id.is_empty() {
            query = query.filter(OrderColumn::Id.eq(form.id));
        }

        if !form.customer_id.is_empty() {
            query = query.filter(OrderColumn::CustomerId.eq(form.customer_id));
        }

        if !form.instrument_id.is_empty() {
            query = query.filter(OrderColumn::InstrumentId.eq(form.instrument_id));
        }

        if !form.software_id.is_empty() {
            query = query.filter(OrderColumn::SoftwareId.eq(form.software_id));
        }

        if !form.express_number.is_empty() {
            query = query.filter(OrderColumn::ExpressNumber.eq(form.express_number));
        }

        if !form.instrument_number.is_empty() {
            query = query.filter(OrderColumn::InstrumentNumber.eq(form.instrument_number));
        }

        if !form.begin_time.is_empty() {
            let begin_time = form.begin_time + " 00:00:00";
            let t = NaiveDateTime::parse_from_str(&begin_time, "%Y-%m-%d %H:%M:%S").unwrap();
            query = query.filter(OrderColumn::CreateTime.gte(t));
        }

        if !form.end_time.is_empty() {
            let end_time = form.end_time + " 23:59:59";
            let t = NaiveDateTime::parse_from_str(&end_time, "%Y-%m-%d %H:%M:%S").unwrap();
            query = query.filter(OrderColumn::CreateTime.lte(t));
        }

        query = query.order_by_desc(OrderColumn::Id);

        query.all(db).await
    }
}

impl SoftwareQuery {
    pub async fn get_software_by_id(
        db: &DbConn,
        id: String,
    ) -> Result<Option<SoftwareModel>, DbErr> {
        SoftwareEntity::find_by_id(id).one(db).await
    }

    pub async fn get_software_in_page(
        db: &DbConn,
        page: u64,
        page_size: u64,
    ) -> Result<(Vec<SoftwareModel>, u64), DbErr> {
        let paginator = SoftwareEntity::find()
            .order_by_asc(SoftwareColumn::Id)
            .paginate(db, page_size);
        let num_pages = paginator.num_pages().await?;

        paginator.fetch_page(page - 1).await.map(|p| (p, num_pages))
    }

    pub async fn search_software(
        db: &DbConn,
        form: SoftwareSearchForm,
    ) -> Result<Vec<SoftwareModel>, DbErr> {
        let mut query = SoftwareEntity::find();

        if !form.id.is_empty() {
            query = query.filter(SoftwareColumn::Id.eq(form.id));
        }

        if !form.package.is_empty() {
            query = query.filter(SoftwareColumn::Package.like(&format!("%{}%", form.package)));
        }

        query = query.order_by_desc(SoftwareColumn::Id);

        query.all(db).await
    }
}
