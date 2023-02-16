pub use super::app::application::{Entity as ApplicationEntity, Model as ApplicationModel};

pub use super::log::{
    log::{Entity as LogEntity, Model as LogModel},
    log_detail::{Entity as LogDetailEntity, Model as LogDetailModel},
    program::{Entity as ProgramEntity, Model as ProgramModel},
};

pub use super::manager::{
    customer::{Entity as CustomerEntity, Model as CustomerModel},
    equipment::{Entity as EquipmentEntity, Model as EquipmentModel},
    product::{Entity as ProductEntity, Model as ProductModel},
    software::{Entity as SoftwareEntity, Model as SoftwareModel},
};
