use super::state::AppState;
use actix_web::{delete, get, post, put, web, Error, HttpResponse, Result};
use zktony_core::{
    ApplicationSearchReq, CustomerGetReq, CustomerSaveReq, DeleteReq, EquipmentGetReq,
    EquipmentSaveReq, LogAddReq, LogDetailAddReq, Mutation, ProductGetReq, ProductSaveReq,
    ProgramAddReq, Query, SoftwareSaveReq, SoftwareGetReq,
};

// region: common
#[get("/")]
async fn index() -> Result<HttpResponse> {
    Ok(HttpResponse::Ok().json("ZkTony-Api"))
}

pub async fn not_found() -> Result<HttpResponse> {
    Ok(HttpResponse::NotFound().json("404 - Not Found"))
}
// endregion

// region: application
#[get("/application")]
async fn get_application_by_id(
    data: web::Data<AppState>,
    req: web::Query<ApplicationSearchReq>,
) -> Result<HttpResponse, Error> {
    let conn = &data.conn;
    let res = Query::get_application_by_id(conn, req.application_id.clone()).await;

    match res {
        Ok(s) => Ok(HttpResponse::Ok().json(s)),
        Err(e) => Ok(HttpResponse::ExpectationFailed().json(e.to_string())),
    }
}
// endregion

// region: customer
#[get("/customer")]
async fn get_customer(
    data: web::Data<AppState>,
    req: web::Query<CustomerGetReq>,
) -> Result<HttpResponse, Error> {
    let conn = &data.conn;
    let res = Query::get_customer(conn, req.0).await;

    match res {
        Ok(s) => Ok(HttpResponse::Ok().json(s)),
        Err(e) => Ok(HttpResponse::ExpectationFailed().json(e.to_string())),
    }
}

#[post("/customer")]
async fn create_customer(
    data: web::Data<AppState>,
    req: web::Json<CustomerSaveReq>,
) -> Result<HttpResponse, Error> {
    let conn = &data.conn;
    let res = Mutation::create_customer(conn, req.clone()).await;

    match res {
        Ok(s) => Ok(HttpResponse::Ok().json(s)),
        Err(e) => Ok(HttpResponse::ExpectationFailed().json(e.to_string())),
    }
}

#[put("/customer")]
async fn update_customer(
    data: web::Data<AppState>,
    req: web::Json<CustomerSaveReq>,
) -> Result<HttpResponse, Error> {
    let conn = &data.conn;
    let res = Mutation::update_customer(conn, req.clone()).await;

    match res {
        Ok(s) => Ok(HttpResponse::Ok().json(s)),
        Err(e) => Ok(HttpResponse::ExpectationFailed().json(e.to_string())),
    }
}

#[delete("/customer")]
async fn delete_customer(
    data: web::Data<AppState>,
    req: web::Json<DeleteReq>,
) -> Result<HttpResponse, Error> {
    let conn = &data.conn;
    let res = Mutation::delete_customer(conn, req.clone()).await;

    match res {
        Ok(s) => Ok(HttpResponse::Ok().json(s)),
        Err(e) => Ok(HttpResponse::ExpectationFailed().json(e.to_string())),
    }
}

// endregion

// region: equipment
#[get("/equipment")]
async fn get_equipment(
    data: web::Data<AppState>,
    req: web::Query<EquipmentGetReq>,
) -> Result<HttpResponse, Error> {
    let conn = &data.conn;
    let res = Query::get_equipment(conn, req.0).await;

    match res {
        Ok(s) => Ok(HttpResponse::Ok().json(s)),
        Err(e) => Ok(HttpResponse::ExpectationFailed().json(e.to_string())),
    }
}

#[post("/equipment")]
async fn create_equipment(
    data: web::Data<AppState>,
    req: web::Json<EquipmentSaveReq>,
) -> Result<HttpResponse, Error> {
    let conn = &data.conn;
    let res = Mutation::create_equipment(conn, req.clone()).await;

    match res {
        Ok(s) => Ok(HttpResponse::Ok().json(s)),
        Err(e) => Ok(HttpResponse::ExpectationFailed().json(e.to_string())),
    }
}

#[put("/equipment")]
async fn update_equipment(
    data: web::Data<AppState>,
    req: web::Json<EquipmentSaveReq>,
) -> Result<HttpResponse, Error> {
    let conn = &data.conn;
    let res = Mutation::update_equipment(conn, req.clone()).await;

    match res {
        Ok(s) => Ok(HttpResponse::Ok().json(s)),
        Err(e) => Ok(HttpResponse::ExpectationFailed().json(e.to_string())),
    }
}

#[delete("/equipment")]
async fn delete_equipment(
    data: web::Data<AppState>,
    req: web::Json<DeleteReq>,
) -> Result<HttpResponse, Error> {
    let conn = &data.conn;
    let res = Mutation::delete_equipment(conn, req.clone()).await;

    match res {
        Ok(s) => Ok(HttpResponse::Ok().json(s)),
        Err(e) => Ok(HttpResponse::ExpectationFailed().json(e.to_string())),
    }
}

// endregion

// region: log
#[post("/log")]
async fn create_log(
    data: web::Data<AppState>,
    req: web::Json<Vec<LogAddReq>>,
) -> Result<HttpResponse, Error> {
    let conn = &data.conn;
    let res = Mutation::create_log(conn, req.clone()).await;

    match res {
        Ok(s) => Ok(HttpResponse::Ok().json(s)),
        Err(e) => Ok(HttpResponse::ExpectationFailed().json(e.to_string())),
    }
}
// endregion

// region: log_detail
#[post("/log/detail")]
async fn create_log_detail(
    data: web::Data<AppState>,
    req: web::Json<Vec<LogDetailAddReq>>,
) -> Result<HttpResponse, Error> {
    let conn = &data.conn;
    let res = Mutation::create_log_detail(conn, req.clone()).await;

    match res {
        Ok(s) => Ok(HttpResponse::Ok().json(s)),
        Err(e) => Ok(HttpResponse::ExpectationFailed().json(e.to_string())),
    }
}
// endregion

// region: product
#[get("/product")]
async fn get_product(
    data: web::Data<AppState>,
    req: web::Query<ProductGetReq>,
) -> Result<HttpResponse, Error> {
    let conn = &data.conn;
    let res = Query::get_product(conn, req.0).await;

    match res {
        Ok(s) => Ok(HttpResponse::Ok().json(s)),
        Err(e) => Ok(HttpResponse::ExpectationFailed().json(e.to_string())),
    }
}

#[post("/product")]
async fn create_product(
    data: web::Data<AppState>,
    req: web::Json<ProductSaveReq>,
) -> Result<HttpResponse, Error> {
    let conn = &data.conn;
    let res = Mutation::create_product(conn, req.clone()).await;

    match res {
        Ok(s) => Ok(HttpResponse::Ok().json(s)),
        Err(e) => Ok(HttpResponse::ExpectationFailed().json(e.to_string())),
    }
}

#[put("/product")]
async fn update_product(
    data: web::Data<AppState>,
    req: web::Json<ProductSaveReq>,
) -> Result<HttpResponse, Error> {
    let conn = &data.conn;
    let res = Mutation::update_product(conn, req.clone()).await;

    match res {
        Ok(s) => Ok(HttpResponse::Ok().json(s)),
        Err(e) => Ok(HttpResponse::ExpectationFailed().json(e.to_string())),
    }
}

#[delete("/product")]
async fn delete_product(
    data: web::Data<AppState>,
    req: web::Json<DeleteReq>,
) -> Result<HttpResponse, Error> {
    let conn = &data.conn;
    let res = Mutation::delete_product(conn, req.clone()).await;

    match res {
        Ok(s) => Ok(HttpResponse::Ok().json(s)),
        Err(e) => Ok(HttpResponse::ExpectationFailed().json(e.to_string())),
    }
}

// endregion

// region: program
#[post("/program")]
async fn create_program(
    data: web::Data<AppState>,
    req: web::Json<Vec<ProgramAddReq>>,
) -> Result<HttpResponse, Error> {
    let conn = &data.conn;
    let res = Mutation::create_program(conn, req.clone()).await;

    match res {
        Ok(s) => Ok(HttpResponse::Ok().json(s)),
        Err(e) => Ok(HttpResponse::ExpectationFailed().json(e.to_string())),
    }
}
// endregion

// region: software
#[get("/software")]
async fn get_software(
    data: web::Data<AppState>,
    req: web::Query<SoftwareGetReq>,
) -> Result<HttpResponse, Error> {
    let conn = &data.conn;
    let res = Query::get_software(conn, req.0).await;

    match res {
        Ok(s) => Ok(HttpResponse::Ok().json(s)),
        Err(e) => Ok(HttpResponse::ExpectationFailed().json(e.to_string())),
    }
}

#[post("/software")]
async fn create_software(
    data: web::Data<AppState>,
    req: web::Json<SoftwareSaveReq>,
) -> Result<HttpResponse, Error> {
    let conn = &data.conn;
    let res = Mutation::create_software(conn, req.clone()).await;

    match res {
        Ok(s) => Ok(HttpResponse::Ok().json(s)),
        Err(e) => Ok(HttpResponse::ExpectationFailed().json(e.to_string())),
    }
}

#[put("/software")]
async fn update_software(
    data: web::Data<AppState>,
    req: web::Json<SoftwareSaveReq>,
) -> Result<HttpResponse, Error> {
    let conn = &data.conn;
    let res = Mutation::update_software(conn, req.clone()).await;

    match res {
        Ok(s) => Ok(HttpResponse::Ok().json(s)),
        Err(e) => Ok(HttpResponse::ExpectationFailed().json(e.to_string())),
    }
}

#[delete("/software")]
async fn delete_software(
    data: web::Data<AppState>,
    req: web::Json<DeleteReq>,
) -> Result<HttpResponse, Error> {
    let conn = &data.conn;
    let res = Mutation::delete_software(conn, req.clone()).await;

    match res {
        Ok(s) => Ok(HttpResponse::Ok().json(s)),
        Err(e) => Ok(HttpResponse::ExpectationFailed().json(e.to_string())),
    }
}
// endregion
