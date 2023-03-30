use ::entity::{
    application::{self, Entity as Application, Model as ApplicationModel},
    log::{self, Entity as Log, Model as LogModel},
    log_detail::{self, Entity as LogDetail, Model as LogDetailModel},
    program::{self, Entity as Program, Model as ProgramModel},
};
use sea_orm::*;

pub struct Mutation;

impl Mutation {
    // region: application
    pub async fn create_application(
        db: &DbConn,
        data: ApplicationModel,
    ) -> Result<ApplicationModel, DbErr> {
        application::ActiveModel {
            application_id: Set(data.application_id),
            build_type: Set(data.build_type),
            download_url: Set(data.download_url),
            version_name: Set(data.version_name),
            version_code: Set(data.version_code),
            description: Set(data.description),
            create_time: Set(data.create_time),
            ..Default::default()
        }
        .insert(db)
        .await
    }

    pub async fn update_application(
        db: &DbConn,
        data: ApplicationModel,
    ) -> Result<ApplicationModel, DbErr> {
        let application: application::ActiveModel = Application::find_by_id(data.id)
            .one(db)
            .await?
            .ok_or(DbErr::Custom("Cannot find application.".to_owned()))
            .map(Into::into)?;

        application::ActiveModel {
            id: application.id,
            application_id: Set(data.application_id),
            build_type: Set(data.build_type),
            download_url: Set(data.download_url),
            version_name: Set(data.version_name),
            version_code: Set(data.version_code),
            description: Set(data.description),
            create_time: Set(data.create_time),
        }
        .update(db)
        .await
    }

    pub async fn delete_application(db: &DbConn, id: i32) -> Result<DeleteResult, DbErr> {
        let application: application::ActiveModel = Application::find_by_id(id)
            .one(db)
            .await?
            .ok_or(DbErr::Custom("Cannot find application.".to_owned()))
            .map(Into::into)?;

        application.delete(db).await
    }
    // endregion

    // region: log
    pub async fn create_log(db: &DbConn, data: LogModel) -> Result<LogModel, DbErr> {
        log::ActiveModel {
            id: Set(data.id),
            sub_id: Set(data.sub_id),
            log_type: Set(data.log_type),
            content: Set(data.content),
            create_time: Set(data.create_time),
            ..Default::default()
        }
        .insert(db)
        .await
    }

    pub async fn create_logs(
        db: &DbConn,
        data: Vec<LogModel>,
    ) -> Result<InsertResult<log::ActiveModel>, DbErr> {
        let mut add_data = Vec::new();
        for req in data {
            let add = log::ActiveModel {
                id: Set(req.id),
                sub_id: Set(req.sub_id),
                log_type: Set(req.log_type),
                content: Set(req.content),
                create_time: Set(req.create_time),
                ..Default::default()
            };
            add_data.push(add);
        }
        Log::insert_many(add_data).exec(db).await
    }

    pub async fn update_log(db: &DbConn, data: LogModel) -> Result<LogModel, DbErr> {
        let log: log::ActiveModel = Log::find_by_id(data.id)
            .one(db)
            .await?
            .ok_or(DbErr::Custom("Cannot find log.".to_owned()))
            .map(Into::into)?;

        log::ActiveModel {
            id: log.id,
            sub_id: Set(data.sub_id),
            log_type: Set(data.log_type),
            content: Set(data.content),
            create_time: Set(data.create_time),
        }
        .update(db)
        .await
    }

    pub async fn delete_log(db: &DbConn, id: String) -> Result<DeleteResult, DbErr> {
        let log: log::ActiveModel = Log::find_by_id(id)
            .one(db)
            .await?
            .ok_or(DbErr::Custom("Cannot find log.".to_owned()))
            .map(Into::into)?;

        log.delete(db).await
    }

    // endregion

    // region: log_detail
    pub async fn create_log_detail(
        db: &DbConn,
        data: LogDetailModel,
    ) -> Result<LogDetailModel, DbErr> {
        log_detail::ActiveModel {
            id: Set(data.id),
            log_id: Set(data.log_id),
            content: Set(data.content),
            create_time: Set(data.create_time),
            ..Default::default()
        }
        .insert(db)
        .await
    }

    pub async fn create_log_details(
        db: &DbConn,
        data: Vec<LogDetailModel>,
    ) -> Result<InsertResult<log_detail::ActiveModel>, DbErr> {
        let mut add_data = Vec::new();
        for req in data {
            let add = log_detail::ActiveModel {
                id: Set(req.id),
                log_id: Set(req.log_id),
                content: Set(req.content),
                create_time: Set(req.create_time),
                ..Default::default()
            };
            add_data.push(add);
        }
        LogDetail::insert_many(add_data).exec(db).await
    }

    pub async fn update_log_detail(
        db: &DbConn,
        data: LogDetailModel,
    ) -> Result<LogDetailModel, DbErr> {
        let log_detail: log_detail::ActiveModel = LogDetail::find_by_id(data.id)
            .one(db)
            .await?
            .ok_or(DbErr::Custom("Cannot find log detail.".to_owned()))
            .map(Into::into)?;

        log_detail::ActiveModel {
            id: log_detail.id,
            log_id: Set(data.log_id),
            content: Set(data.content),
            create_time: Set(data.create_time),
        }
        .update(db)
        .await
    }

    pub async fn delete_log_detail(db: &DbConn, id: String) -> Result<DeleteResult, DbErr> {
        let log_detail: log_detail::ActiveModel = LogDetail::find_by_id(id)
            .one(db)
            .await?
            .ok_or(DbErr::Custom("Cannot find log detail.".to_owned()))
            .map(Into::into)?;

        log_detail.delete(db).await
    }

    // endregion

    // region: program
    pub async fn create_program(db: &DbConn, data: ProgramModel) -> Result<ProgramModel, DbErr> {
        program::ActiveModel {
            id: Set(data.id),
            name: Set(data.name),
            content: Set(data.content),
            create_time: Set(data.create_time),
            ..Default::default()
        }
        .insert(db)
        .await
    }

    pub async fn create_programs(
        db: &DbConn,
        data: Vec<ProgramModel>,
    ) -> Result<InsertResult<program::ActiveModel>, DbErr> {
        let mut add_data = Vec::new();
        for req in data {
            let add = program::ActiveModel {
                id: Set(req.id),
                name: Set(req.name),
                content: Set(req.content),
                create_time: Set(req.create_time),
                ..Default::default()
            };
            add_data.push(add);
        }
        Program::insert_many(add_data).exec(db).await
    }

    pub async fn update_program(db: &DbConn, data: ProgramModel) -> Result<ProgramModel, DbErr> {
        let program: program::ActiveModel = Program::find_by_id(data.id)
            .one(db)
            .await?
            .ok_or(DbErr::Custom("Cannot find program.".to_owned()))
            .map(Into::into)?;

        program::ActiveModel {
            id: program.id,
            name: Set(data.name),
            content: Set(data.content),
            create_time: Set(data.create_time),
        }
        .update(db)
        .await
    }

    pub async fn delete_program(db: &DbConn, id: String) -> Result<DeleteResult, DbErr> {
        let program: program::ActiveModel = Program::find_by_id(id)
            .one(db)
            .await?
            .ok_or(DbErr::Custom("Cannot find program.".to_owned()))
            .map(Into::into)?;

        program.delete(db).await
    }
    // endregion
}
