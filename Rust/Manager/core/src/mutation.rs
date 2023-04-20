use ::entity::*;
use sea_orm::*;

pub struct CustomerMutation;

pub struct InstrumentMutation;

pub struct OrderMutation;

pub struct SoftwareMutation;

impl CustomerMutation {
    pub async fn create_customer(db: &DbConn, data: CustomerModel) -> Result<CustomerModel, DbErr> {
        CustomerActiveModel {
            id: Set(data.id),
            name: Set(data.name),
            address: Set(data.address),
            phone: Set(data.phone),
            source: Set(data.source),
            industry: Set(data.industry),
            remarks: Set(data.remarks),
            create_by: Set(data.create_by),
            create_time: Set(data.create_time),
            ..Default::default()
        }
        .insert(db)
        .await
    }

    pub async fn update_customer(db: &DbConn, data: CustomerModel) -> Result<CustomerModel, DbErr> {
        let customer: CustomerActiveModel = CustomerEntity::find_by_id(data.id)
            .one(db)
            .await?
            .ok_or(DbErr::Custom("Cannot find customer".to_owned()))
            .map(Into::into)?;

        CustomerActiveModel {
            id: customer.id,
            name: Set(data.name),
            address: Set(data.address),
            phone: Set(data.phone),
            source: Set(data.source),
            industry: Set(data.industry),
            remarks: Set(data.remarks),
            create_by: Set(data.create_by),
            create_time: Set(data.create_time),
        }
        .update(db)
        .await
    }

    pub async fn delete_customer(db: &DbConn, id: String) -> Result<DeleteResult, DbErr> {
        let customer: CustomerActiveModel = CustomerEntity::find_by_id(id)
            .one(db)
            .await?
            .ok_or(DbErr::Custom("Cannot find customer".to_owned()))
            .map(Into::into)?;

        customer.delete(db).await
    }
}

impl InstrumentMutation {
    pub async fn create_instrument(
        db: &DbConn,
        data: InstrumentModel,
    ) -> Result<InstrumentModel, DbErr> {
        InstrumentActiveModel {
            id: Set(data.id),
            name: Set(data.name),
            model: Set(data.model),
            voltage: Set(data.voltage),
            power: Set(data.power),
            frequency: Set(data.frequency),
            attachment: Set(data.attachment),
            remarks: Set(data.remarks),
            create_by: Set(data.create_by),
            create_time: Set(data.create_time),
            ..Default::default()
        }
        .insert(db)
        .await
    }

    pub async fn update_instrument(
        db: &DbConn,
        data: InstrumentModel,
    ) -> Result<InstrumentModel, DbErr> {
        let instrument: InstrumentActiveModel = InstrumentEntity::find_by_id(data.id)
            .one(db)
            .await?
            .ok_or(DbErr::Custom("Cannot find instrument".to_owned()))
            .map(Into::into)?;

        InstrumentActiveModel {
            id: instrument.id,
            name: Set(data.name),
            model: Set(data.model),
            voltage: Set(data.voltage),
            power: Set(data.power),
            frequency: Set(data.frequency),
            attachment: Set(data.attachment),
            remarks: Set(data.remarks),
            create_by: Set(data.create_by),
            create_time: Set(data.create_time),
        }
        .update(db)
        .await
    }

    pub async fn delete_instrument(db: &DbConn, id: String) -> Result<DeleteResult, DbErr> {
        let instrument: InstrumentActiveModel = InstrumentEntity::find_by_id(id)
            .one(db)
            .await?
            .ok_or(DbErr::Custom("Cannot find instrument".to_owned()))
            .map(Into::into)?;

        instrument.delete(db).await
    }
}

impl OrderMutation {
    pub async fn create_order(db: &DbConn, data: OrderModel) -> Result<OrderModel, DbErr> {
        OrderActiveModel {
            id: Set(data.id),
            customer_id: Set(data.customer_id),
            instrument_id: Set(data.instrument_id),
            software_id: Set(data.software_id),
            express_number: Set(data.express_number),
            express_company: Set(data.express_company),
            instrument_number: Set(data.instrument_number),
            instrument_time: Set(data.instrument_time),
            attachment: Set(data.attachment),
            remarks: Set(data.remarks),
            create_by: Set(data.create_by),
            create_time: Set(data.create_time),
            ..Default::default()
        }
        .insert(db)
        .await
    }

    pub async fn update_order(db: &DbConn, data: OrderModel) -> Result<OrderModel, DbErr> {
        let order: OrderActiveModel = OrderEntity::find_by_id(data.id)
            .one(db)
            .await?
            .ok_or(DbErr::Custom("Cannot find order".to_owned()))
            .map(Into::into)?;

        OrderActiveModel {
            id: order.id,
            customer_id: Set(data.customer_id),
            instrument_id: Set(data.instrument_id),
            software_id: Set(data.software_id),
            express_number: Set(data.express_number),
            express_company: Set(data.express_company),
            instrument_number: Set(data.instrument_number),
            instrument_time: Set(data.instrument_time),
            attachment: Set(data.attachment),
            remarks: Set(data.remarks),
            create_by: Set(data.create_by),
            create_time: Set(data.create_time),
        }
        .update(db)
        .await
    }

    pub async fn delete_order(db: &DbConn, id: String) -> Result<DeleteResult, DbErr> {
        let order: OrderActiveModel = OrderEntity::find_by_id(id)
            .one(db)
            .await?
            .ok_or(DbErr::Custom("Cannot find order".to_owned()))
            .map(Into::into)?;

        order.delete(db).await
    }
}

impl SoftwareMutation {
    pub async fn create_software(db: &DbConn, data: SoftwareModel) -> Result<SoftwareModel, DbErr> {
        SoftwareActiveModel {
            id: Set(data.id),
            package: Set(data.package),
            version_name: Set(data.version_name),
            version_code: Set(data.version_code),
            build_type: Set(data.build_type),
            remarks: Set(data.remarks),
            create_by: Set(data.create_by),
            create_time: Set(data.create_time),
            ..Default::default()
        }
        .insert(db)
        .await
    }

    pub async fn update_software(db: &DbConn, data: SoftwareModel) -> Result<SoftwareModel, DbErr> {
        let software: SoftwareActiveModel = SoftwareEntity::find_by_id(data.id)
            .one(db)
            .await?
            .ok_or(DbErr::Custom("Cannot find software".to_owned()))
            .map(Into::into)?;

        SoftwareActiveModel {
            id: software.id,
            package: Set(data.package),
            version_name: Set(data.version_name),
            version_code: Set(data.version_code),
            build_type: Set(data.build_type),
            remarks: Set(data.remarks),
            create_by: Set(data.create_by),
            create_time: Set(data.create_time),
        }
        .update(db)
        .await
    }

    pub async fn delete_software(db: &DbConn, id: String) -> Result<DeleteResult, DbErr> {
        let software: SoftwareActiveModel = SoftwareEntity::find_by_id(id)
            .one(db)
            .await?
            .ok_or(DbErr::Custom("Cannot find software".to_owned()))
            .map(Into::into)?;

        software.delete(db).await
    }
}
