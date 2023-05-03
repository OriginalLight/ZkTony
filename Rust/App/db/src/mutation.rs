use ::entity::prelude::*;
use sea_orm::*;

pub struct ApplicationMutation;
pub struct LogMutation;
pub struct LogDetailMutation;
pub struct ProgramMutation;

impl ApplicationMutation {
    pub async fn insert(db: &DbConn, data: ApplicationModel) -> Result<ApplicationModel, DbErr> {
        ApplicationActiveModel {
            id: Set(data.id),
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

    pub async fn insert_batch(db: &DbConn, data: Vec<ApplicationModel>) -> Result<(), DbErr> {
        let txn = db.begin().await?;

        let mut models = Vec::new();
        for model in data {
            models.push(ApplicationActiveModel {
                id: Set(model.id),
                application_id: Set(model.application_id),
                build_type: Set(model.build_type),
                download_url: Set(model.download_url),
                version_name: Set(model.version_name),
                version_code: Set(model.version_code),
                description: Set(model.description),
                create_time: Set(model.create_time),
                ..Default::default()
            })
        }
        Application::insert_many(models).exec(&txn).await?;

        txn.commit().await
    }

    pub async fn update(db: &DbConn, data: ApplicationModel) -> Result<ApplicationModel, DbErr> {
        let application: ApplicationActiveModel = Application::find_by_id(data.id)
            .one(db)
            .await?
            .ok_or(DbErr::Custom("Cannot find application".to_owned()))
            .map(Into::into)?;

        ApplicationActiveModel {
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

    pub async fn update_batch(db: &DbConn, data: Vec<ApplicationModel>) -> Result<(), DbErr> {
        let txn = db.begin().await?;

        for model in data {
            ApplicationMutation::update(db, model).await?;
        }

        txn.commit().await
    }

    pub async fn delete(db: &DbConn, data: ApplicationModel) -> Result<DeleteResult, DbErr> {
        let application: ApplicationActiveModel = Application::find_by_id(data.id)
            .one(db)
            .await?
            .ok_or(DbErr::Custom("Cannot find application".to_owned()))
            .map(Into::into)?;

        application.delete(db).await
    }

    pub async fn delete_batch(db: &DbConn, data: Vec<ApplicationModel>) -> Result<(), DbErr> {
        let txn = db.begin().await?;

        for model in data {
            ApplicationMutation::delete(db, model).await?;
        }

        txn.commit().await
    }
}

impl LogMutation {
    pub async fn insert(db: &DbConn, data: LogModel) -> Result<LogModel, DbErr> {
        LogActiveModel {
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

    pub async fn insert_batch(
        db: &DbConn,
        data: Vec<LogModel>,
    ) -> Result<InsertResult<LogActiveModel>, DbErr> {
        let mut add_data = Vec::new();
        for req in data {
            let add = LogActiveModel {
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

    pub async fn update(db: &DbConn, data: LogModel) -> Result<LogModel, DbErr> {
        let log: LogActiveModel = Log::find_by_id(data.id)
            .one(db)
            .await?
            .ok_or(DbErr::Custom("Cannot find log".to_owned()))
            .map(Into::into)?;

        LogActiveModel {
            id: log.id,
            sub_id: Set(data.sub_id),
            log_type: Set(data.log_type),
            content: Set(data.content),
            create_time: Set(data.create_time),
        }
        .update(db)
        .await
    }

    pub async fn update_batch(db: &DbConn, data: Vec<LogModel>) -> Result<(), DbErr> {
        let txn = db.begin().await?;

        for model in data {
            LogMutation::update(db, model).await?;
        }

        txn.commit().await
    }

    pub async fn delete(db: &DbConn, data: LogModel) -> Result<DeleteResult, DbErr> {
        let log: LogActiveModel = Log::find_by_id(data.id)
            .one(db)
            .await?
            .ok_or(DbErr::Custom("Cannot find log".to_owned()))
            .map(Into::into)?;

        log.delete(db).await
    }

    pub async fn delete_batch(db: &DbConn, data: Vec<LogModel>) -> Result<(), DbErr> {
        let txn = db.begin().await?;

        for model in data {
            LogMutation::delete(db, model).await?;
        }

        txn.commit().await
    }
}

impl LogDetailMutation {
    pub async fn insert(db: &DbConn, data: LogDetailModel) -> Result<LogDetailModel, DbErr> {
        LogDetailActiveModel {
            id: Set(data.id),
            log_id: Set(data.log_id),
            content: Set(data.content),
            create_time: Set(data.create_time),
            ..Default::default()
        }
        .insert(db)
        .await
    }

    pub async fn insert_batch(
        db: &DbConn,
        data: Vec<LogDetailModel>,
    ) -> Result<InsertResult<LogDetailActiveModel>, DbErr> {
        let mut add_data = Vec::new();
        for req in data {
            let add = LogDetailActiveModel {
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

    pub async fn update(db: &DbConn, data: LogDetailModel) -> Result<LogDetailModel, DbErr> {
        let log_detail: LogDetailActiveModel = LogDetail::find_by_id(data.id)
            .one(db)
            .await?
            .ok_or(DbErr::Custom("Cannot find log detail".to_owned()))
            .map(Into::into)?;

        LogDetailActiveModel {
            id: log_detail.id,
            log_id: Set(data.log_id),
            content: Set(data.content),
            create_time: Set(data.create_time),
        }
        .update(db)
        .await
    }

    pub async fn update_batch(db: &DbConn, data: Vec<LogDetailModel>) -> Result<(), DbErr> {
        let txn = db.begin().await?;

        for model in data {
            LogDetailMutation::update(db, model).await?;
        }

        txn.commit().await
    }

    pub async fn delete(db: &DbConn, data: LogDetailModel) -> Result<DeleteResult, DbErr> {
        let log_detail: LogDetailActiveModel = LogDetail::find_by_id(data.id)
            .one(db)
            .await?
            .ok_or(DbErr::Custom("Cannot find log detail".to_owned()))
            .map(Into::into)?;

        log_detail.delete(db).await
    }

    pub async fn delete_batch(db: &DbConn, data: Vec<LogDetailModel>) -> Result<(), DbErr> {
        let txn = db.begin().await?;

        for model in data {
            LogDetailMutation::delete(db, model).await?;
        }

        txn.commit().await
    }
}

impl ProgramMutation {
    pub async fn insert(db: &DbConn, data: ProgramModel) -> Result<ProgramModel, DbErr> {
        ProgramActiveModel {
            id: Set(data.id),
            name: Set(data.name),
            content: Set(data.content),
            create_time: Set(data.create_time),
            ..Default::default()
        }
        .insert(db)
        .await
    }

    pub async fn insert_batch(
        db: &DbConn,
        data: Vec<ProgramModel>,
    ) -> Result<InsertResult<ProgramActiveModel>, DbErr> {
        let mut add_data = Vec::new();
        for req in data {
            let add = ProgramActiveModel {
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

    pub async fn update(db: &DbConn, data: ProgramModel) -> Result<ProgramModel, DbErr> {
        let program: ProgramActiveModel = Program::find_by_id(data.id)
            .one(db)
            .await?
            .ok_or(DbErr::Custom("Cannot find program".to_owned()))
            .map(Into::into)?;

        ProgramActiveModel {
            id: program.id,
            name: Set(data.name),
            content: Set(data.content),
            create_time: Set(data.create_time),
        }
        .update(db)
        .await
    }

    pub async fn update_batch(db: &DbConn, data: Vec<ProgramModel>) -> Result<(), DbErr> {
        let txn = db.begin().await?;

        for model in data {
            ProgramMutation::update(db, model).await?;
        }

        txn.commit().await
    }

    pub async fn delete(db: &DbConn, data: ProgramModel) -> Result<DeleteResult, DbErr> {
        let program: ProgramActiveModel = Program::find_by_id(data.id)
            .one(db)
            .await?
            .ok_or(DbErr::Custom("Cannot find program".to_owned()))
            .map(Into::into)?;

        program.delete(db).await
    }

    pub async fn delete_batch(db: &DbConn, data: Vec<ProgramModel>) -> Result<(), DbErr> {
        let txn = db.begin().await?;

        for model in data {
            ProgramMutation::delete(db, model).await?;
        }

        txn.commit().await
    }
}
