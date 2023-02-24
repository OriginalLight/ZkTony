use ::entity::{
    application,
    application::{Entity as ApplicationEntity, Model as ApplicationModel},
    customer,
    customer::{Entity as CustomerEntity, Model as CustomerModel},
    equipment,
    equipment::{Entity as EquipmentEntity, Model as EquipmentModel},
    product,
    product::{Entity as ProductEntity, Model as ProductModel},
    software,
    software::{Entity as SoftwareEntity, Model as SoftwareModel},
};
use chrono::NaiveDateTime;
use sea_orm::*;

use crate::{CustomerGetReq, EquipmentGetReq, ProductGetReq, SoftwareGetReq};

pub struct Query;

impl Query {
    // region: application
    pub async fn get_application_by_id(
        db: &DbConn,
        id: String,
    ) -> Result<Option<ApplicationModel>, DbErr> {
        ApplicationEntity::find()
            .filter(application::Column::ApplicationId.eq(id))
            .one(db)
            .await
    }
    // endregion

    // region: customer
    pub async fn get_customer(
        db: &DbConn,
        req: CustomerGetReq,
    ) -> Result<Option<Vec<CustomerModel>>, DbErr> {
        let mut query = CustomerEntity::find();
        if let Some(x) = req.id {
            if !x.is_empty() {
                query = query.filter(customer::Column::Id.eq(x));
            }
        }

        if let Some(x) = req.name {
            if !x.is_empty() {
                query = query.filter(customer::Column::Name.like(&format!("%{}%", x)));
            }
        }

        if let Some(x) = req.address {
            if !x.is_empty() {
                query = query.filter(customer::Column::Address.like(&format!("%{}%", x)));
            }
        }

        if let Some(x) = req.phone {
            if !x.is_empty() {
                query = query.filter(customer::Column::Phone.like(&format!("%{}%", x)));
            }
        }

        let res = query.clone().all(db).await;

        match res {
            Ok(x) => {
                if x.is_empty() {
                    Ok(None)
                } else {
                    Ok(Some(x))
                }
            }
            Err(e) => Err(e),
        }
    }
    // endregion

    // region: equipment
    pub async fn get_equipment(
        db: &DbConn,
        req: EquipmentGetReq,
    ) -> Result<Option<Vec<EquipmentModel>>, DbErr> {
        let mut query = EquipmentEntity::find();
        if let Some(x) = req.id {
            if !x.is_empty() {
                query = query.filter(equipment::Column::Id.eq(x));
            }
        }

        if let Some(x) = req.name {
            if !x.is_empty() {
                query = query.filter(equipment::Column::Name.like(&format!("%{}%", x)));
            }
        }

        if let Some(x) = req.model {
            if !x.is_empty() {
                query = query.filter(equipment::Column::Model.like(&format!("%{}%", x)));
            }
        }

        if let Some(x) = req.begin_time {
            if !x.is_empty() {
                let x = x + " 00:00:00";
                let t = NaiveDateTime::parse_from_str(&x, "%Y-%m-%d").unwrap();
                query = query.filter(equipment::Column::CreateTime.gte(t));
            }
        }
        if let Some(x) = req.end_time {
            if !x.is_empty() {
                let x = x + " 23:59:59";
                let t = NaiveDateTime::parse_from_str(&x, "%Y-%m-%d").unwrap();
                query = query.filter(equipment::Column::CreateTime.lte(t));
            }
        }

        let res = query.clone().all(db).await;

        match res {
            Ok(x) => {
                if x.is_empty() {
                    Ok(None)
                } else {
                    Ok(Some(x))
                }
            }
            Err(e) => Err(e),
        }
    }

    // endregion

    // region: product

    pub async fn get_product(
        db: &DbConn,
        req: ProductGetReq,
    ) -> Result<Option<Vec<ProductModel>>, DbErr> {
        let mut query = ProductEntity::find();
        if let Some(x) = req.id {
            if !x.is_empty() {
                query = query.filter(product::Column::Id.eq(x));
            }
        }

        if let Some(x) = req.software_id {
            if !x.is_empty() {
                query = query.filter(product::Column::SoftwareId.eq(x));
            }
        }

        if let Some(x) = req.customer_id {
            if !x.is_empty() {
                query = query.filter(product::Column::CustomerId.eq(x));
            }
        }

        if let Some(x) = req.equipment_id {
            if !x.is_empty() {
                query = query.filter(product::Column::EquipmentId.eq(x));
            }
        }

        if let Some(x) = req.express_number {
            if !x.is_empty() {
                query = query.filter(product::Column::ExpressNumber.like(&format!("%{}%", x)));
            }
        }

        if let Some(x) = req.express_company {
            if !x.is_empty() {
                query = query.filter(product::Column::ExpressCompany.like(&format!("%{}%", x)));
            }
        }

        if let Some(x) = req.equipment_number {
            if !x.is_empty() {
                query = query.filter(product::Column::EquipmentNumber.eq(x));
            }
        }

        if let Some(x) = req.create_by {
            if !x.is_empty() {
                query = query.filter(product::Column::CreateBy.eq(x));
            }
        }

        if let Some(x) = req.begin_time {
            if !x.is_empty() {
                let x = x + " 00:00:00";
                let t = NaiveDateTime::parse_from_str(&x, "%Y-%m-%d").unwrap();
                query = query.filter(product::Column::CreateTime.gte(t));
            }
        }
        if let Some(x) = req.end_time {
            if !x.is_empty() {
                let x = x + " 23:59:59";
                let t = NaiveDateTime::parse_from_str(&x, "%Y-%m-%d").unwrap();
                query = query.filter(product::Column::CreateTime.lte(t));
            }
        }

        let res = query.clone().all(db).await;
        match res {
            Ok(x) => {
                if x.is_empty() {
                    Ok(None)
                } else {
                    Ok(Some(x))
                }
            }
            Err(e) => Err(e),
        }
    }
    // endregion

    // region: software

    pub async fn get_software(
        db: &DbConn,
        req: SoftwareGetReq,
    ) -> Result<Option<Vec<SoftwareModel>>, DbErr> {
        let mut query = SoftwareEntity::find();
        if let Some(x) = req.id {
            if !x.is_empty() {
                query = query.filter(software::Column::Id.eq(x));
            }
        }

        if let Some(x) = req.package {
            if !x.is_empty() {
                query = query.filter(software::Column::Package.eq(x));
            }
        }

        if let Some(x) = req.build_type {
            if !x.is_empty() {
                query = query.filter(software::Column::BuildType.eq(x));
            }
        }

        let res = query.clone().all(db).await;
        match res {
            Ok(x) => {
                if x.is_empty() {
                    Ok(None)
                } else {
                    Ok(Some(x))
                }
            }
            Err(e) => Err(e),
        }
    }

    // endregion
}
