use actix_web::web;

use super::handler;

pub fn init(cfg: &mut web::ServiceConfig) {
    cfg.service(handler::index);
    cfg.service(handler::get_application_by_id);
    cfg.service(handler::get_customer);
    cfg.service(handler::create_customer);
    cfg.service(handler::update_customer);
    cfg.service(handler::delete_customer);
    cfg.service(handler::get_equipment);
    cfg.service(handler::create_equipment);
    cfg.service(handler::update_equipment);
    cfg.service(handler::delete_equipment);
    cfg.service(handler::create_log);
    cfg.service(handler::create_log_detail);
    cfg.service(handler::get_product);
    cfg.service(handler::create_product);
    cfg.service(handler::update_product);
    cfg.service(handler::delete_product);
    cfg.service(handler::create_program);
    cfg.service(handler::get_software);
    cfg.service(handler::create_software);
    cfg.service(handler::update_software);
    cfg.service(handler::delete_software);
}
