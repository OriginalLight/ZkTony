use ::entity::{
    customer,
    customer::{Column as CustomerColumn, Entity as CustomerEntity},
    equipment,
    equipment::{Column as EquipmentColumn, Entity as EquipmentEntity},
    log,
    log::{Column as LogColumn, Entity as LogEntity},
    log_detail,
    log_detail::{Column as LogDetailColumn, Entity as LogDetailEntity},
    product,
    product::{Column as ProductColumn, Entity as ProductEntity},
    program,
    program::{Column as ProgramColumn, Entity as ProgramEntity},
    software,
    software::{Column as SoftwareColumn, Entity as SoftwareEntity},
};
use chrono::NaiveDateTime;
use sea_orm::{sea_query::OnConflict, *};

use crate::{
    CustomerSaveReq, DeleteReq, EquipmentSaveReq, LogAddReq, LogDetailAddReq, ProductSaveReq,
    ProgramAddReq, SoftwareSaveReq,
};

pub struct Mutation;

impl Mutation {
    // region: log_detail
    pub async fn create_log_detail(
        db: &DbConn,
        reqs: Vec<LogDetailAddReq>,
    ) -> Result<String, DbErr> {
        let mut add_data = Vec::new();
        for req in reqs {
            let create_time =
                NaiveDateTime::parse_from_str(&req.create_time.unwrap(), "%Y-%m-%d %H:%M:%S")
                    .unwrap();
            let add = log_detail::ActiveModel {
                id: Set(req.id),
                log_id: Set(req.log_id),
                content: Set(req.content),
                create_time: Set(Some(create_time)),
                ..Default::default()
            };
            add_data.push(add);
        }
        let res = LogDetailEntity::insert_many(add_data)
            .on_conflict(
                OnConflict::column(LogDetailColumn::Id)
                    .do_nothing()
                    .to_owned(),
            )
            .exec(db)
            .await;

        match res {
            Ok(_) => Ok("Add Success!!".to_string()),
            Err(e) => Err(e),
        }
    }
    // endregion

    // region: log
    pub async fn create_log(db: &DbConn, reqs: Vec<LogAddReq>) -> Result<String, DbErr> {
        let mut add_data = Vec::new();
        for req in reqs {
            let create_time =
                NaiveDateTime::parse_from_str(&req.create_time.unwrap(), "%Y-%m-%d %H:%M:%S")
                    .unwrap();
            let add = log::ActiveModel {
                id: Set(req.id),
                sub_id: Set(req.sub_id),
                log_type: Set(req.log_type),
                content: Set(req.content),
                create_time: Set(Some(create_time)),
                ..Default::default()
            };
            add_data.push(add);
        }
        let res = LogEntity::insert_many(add_data)
            .on_conflict(OnConflict::column(LogColumn::Id).do_nothing().to_owned())
            .exec(db)
            .await;

        match res {
            Ok(_) => Ok("Add Success!!".to_string()),
            Err(e) => Err(e),
        }
    }
    // endregion

    // region: program
    pub async fn create_program(db: &DbConn, reqs: Vec<ProgramAddReq>) -> Result<String, DbErr> {
        let mut add_data = Vec::new();
        for req in reqs {
            let create_time =
                NaiveDateTime::parse_from_str(&req.create_time.unwrap(), "%Y-%m-%d %H:%M:%S")
                    .unwrap();
            let add = program::ActiveModel {
                id: Set(req.id),
                name: Set(req.name),
                content: Set(req.content),
                create_time: Set(Some(create_time)),
                ..Default::default()
            };
            add_data.push(add);
        }
        let res = ProgramEntity::insert_many(add_data)
            .on_conflict(
                OnConflict::column(ProgramColumn::Id)
                    .do_nothing()
                    .to_owned(),
            )
            .exec(db)
            .await;

        match res {
            Ok(_) => Ok("Success".to_string()),
            Err(e) => Err(e),
        }
    }

    // endregion

    // region: customer
    pub async fn create_customer(db: &DbConn, req: CustomerSaveReq) -> Result<String, DbErr> {
        let create_time =
            NaiveDateTime::parse_from_str(&req.create_time.unwrap(), "%Y-%m-%d %H:%M:%S").unwrap();
        let add_data = customer::ActiveModel {
            id: Set(req.id),
            name: Set(req.name),
            address: Set(req.address),
            phone: Set(req.phone),
            source: Set(req.source),
            industry: Set(req.industry),
            remarks: Set(req.remarks),
            create_by: Set(req.create_by),
            create_time: Set(Some(create_time)),
            ..Default::default()
        };
        let res = CustomerEntity::insert(add_data)
            .on_conflict(
                OnConflict::column(CustomerColumn::Id)
                    .do_nothing()
                    .to_owned(),
            )
            .exec(db)
            .await;

        match res {
            Ok(_) => Ok("Add Success!!".to_string()),
            Err(e) => Err(e),
        }
    }

    pub async fn update_customer(db: &DbConn, req: CustomerSaveReq) -> Result<String, DbErr> {
        let update_data = customer::ActiveModel {
            id: Set(req.id),
            name: Set(req.name),
            address: Set(req.address),
            phone: Set(req.phone),
            source: Set(req.source),
            industry: Set(req.industry),
            remarks: Set(req.remarks),
            ..Default::default()
        };
        let res = CustomerEntity::update(update_data).exec(db).await;

        match res {
            Ok(_) => Ok("Update Success!!".to_string()),
            Err(e) => Err(e),
        }
    }

    pub async fn delete_customer(db: &DbConn, req: DeleteReq) -> Result<String, DbErr> {
        let res = CustomerEntity::delete_by_id(req.id).exec(db).await;
        match res {
            Ok(_) => Ok("Delete Success!!".to_string()),
            Err(e) => Err(e),
        }
    }

    // endregion

    // region: software
    pub async fn create_software(db: &DbConn, req: SoftwareSaveReq) -> Result<String, DbErr> {
        let create_time =
            NaiveDateTime::parse_from_str(&req.create_time.unwrap(), "%Y-%m-%d %H:%M:%S").unwrap();
        let add_data = software::ActiveModel {
            id: Set(req.id),
            package: Set(req.package),
            version_code: Set(req.version_code),
            version_name: Set(req.version_name),
            build_type: Set(req.build_type),
            remarks: Set(req.remarks),
            create_time: Set(Some(create_time)),
            ..Default::default()
        };
        let res = SoftwareEntity::insert(add_data)
            .on_conflict(
                OnConflict::column(SoftwareColumn::Id)
                    .do_nothing()
                    .to_owned(),
            )
            .exec(db)
            .await;

        match res {
            Ok(_) => Ok("Add Success!!".to_string()),
            Err(e) => Err(e),
        }
    }

    pub async fn update_software(db: &DbConn, req: SoftwareSaveReq) -> Result<String, DbErr> {
        let update_data = software::ActiveModel {
            id: Set(req.id),
            package: Set(req.package),
            version_code: Set(req.version_code),
            version_name: Set(req.version_name),
            build_type: Set(req.build_type),
            remarks: Set(req.remarks),
            ..Default::default()
        };
        let res = SoftwareEntity::update(update_data).exec(db).await;

        match res {
            Ok(_) => Ok("Update Success!!".to_string()),
            Err(e) => Err(e),
        }
    }

    pub async fn delete_software(db: &DbConn, req: DeleteReq) -> Result<String, DbErr> {
        let res = SoftwareEntity::delete_by_id(req.id).exec(db).await;
        match res {
            Ok(_) => Ok("Delete Success!!".to_string()),
            Err(e) => Err(e),
        }
    }

    // endregion

    // region: equipment
    pub async fn create_equipment(db: &DbConn, req: EquipmentSaveReq) -> Result<String, DbErr> {
        let create_time =
            NaiveDateTime::parse_from_str(&req.create_time.unwrap(), "%Y-%m-%d %H:%M:%S").unwrap();
        let add_data = equipment::ActiveModel {
            id: Set(req.id),
            name: Set(req.name),
            model: Set(req.model),
            voltage: Set(req.voltage),
            power: Set(req.power),
            frequency: Set(req.frequency),
            attachment: Set(req.attachment),
            remarks: Set(req.remarks),
            create_by: Set(req.create_by),
            create_time: Set(Some(create_time)),
            ..Default::default()
        };
        let res = EquipmentEntity::insert(add_data)
            .on_conflict(
                OnConflict::column(EquipmentColumn::Id)
                    .do_nothing()
                    .to_owned(),
            )
            .exec(db)
            .await;

        match res {
            Ok(_) => Ok("Add Success!!".to_string()),
            Err(e) => Err(e),
        }
    }

    pub async fn update_equipment(db: &DbConn, req: EquipmentSaveReq) -> Result<String, DbErr> {
        let update_data = equipment::ActiveModel {
            id: Set(req.id),
            name: Set(req.name),
            model: Set(req.model),
            voltage: Set(req.voltage),
            power: Set(req.power),
            frequency: Set(req.frequency),
            attachment: Set(req.attachment),
            remarks: Set(req.remarks),
            create_by: Set(req.create_by),
            ..Default::default()
        };
        let res = EquipmentEntity::update(update_data).exec(db).await;

        match res {
            Ok(_) => Ok("Update Success!!".to_string()),
            Err(e) => Err(e),
        }
    }

    pub async fn delete_equipment(db: &DbConn, req: DeleteReq) -> Result<String, DbErr> {
        let res = EquipmentEntity::delete_by_id(req.id).exec(db).await;
        match res {
            Ok(_) => Ok("Delete Success!!".to_string()),
            Err(e) => Err(e),
        }
    }

    // endregion

    // region: product

    pub async fn create_product(db: &DbConn, req: ProductSaveReq) -> Result<String, DbErr> {
        let create_time =
            NaiveDateTime::parse_from_str(&req.create_time.unwrap(), "%Y-%m-%d %H:%M:%S").unwrap();
        let equipment_time =
            NaiveDateTime::parse_from_str(&req.equipment_time, "%Y-%m-%d").unwrap();
        let add_data = product::ActiveModel {
            id: Set(req.id),
            software_id: Set(req.software_id),
            customer_id: Set(req.customer_id),
            equipment_id: Set(req.equipment_id),
            express_number: Set(req.express_number),
            express_company: Set(req.express_company),
            equipment_number: Set(req.equipment_number),
            equipment_time: Set(equipment_time),
            attachment: Set(req.attachment),
            remarks: Set(req.remarks),
            create_by: Set(req.create_by),
            create_time: Set(Some(create_time)),
            ..Default::default()
        };
        let res = ProductEntity::insert(add_data)
            .on_conflict(
                OnConflict::column(ProductColumn::Id)
                    .do_nothing()
                    .to_owned(),
            )
            .exec(db)
            .await;

        match res {
            Ok(_) => Ok("Add Success!!".to_string()),
            Err(e) => Err(e),
        }
    }

    pub async fn update_product(db: &DbConn, req: ProductSaveReq) -> Result<String, DbErr> {
        let equipment_time =
            NaiveDateTime::parse_from_str(&req.equipment_time, "%Y-%m-%d").unwrap();
        let update_data = product::ActiveModel {
            id: Set(req.id),
            software_id: Set(req.software_id),
            customer_id: Set(req.customer_id),
            equipment_id: Set(req.equipment_id),
            express_number: Set(req.express_number),
            express_company: Set(req.express_company),
            equipment_number: Set(req.equipment_number),
            equipment_time: Set(equipment_time),
            attachment: Set(req.attachment),
            remarks: Set(req.remarks),
            create_by: Set(req.create_by),
            ..Default::default()
        };
        let res = ProductEntity::update(update_data).exec(db).await;

        match res {
            Ok(_) => Ok("Update Success!!".to_string()),
            Err(e) => Err(e),
        }
    }

    pub async fn delete_product(db: &DbConn, req: DeleteReq) -> Result<String, DbErr> {
        let res = ProductEntity::delete_by_id(req.id).exec(db).await;
        match res {
            Ok(_) => Ok("Delete Success!!".to_string()),
            Err(e) => Err(e),
        }
    }

    // endregion
}
