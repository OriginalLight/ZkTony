use ::entity::{
    application::{
        self, Column as ApplicationColumn, Entity as Application, Model as ApplicationModel,
    },
    log::{self, Column as LogColumn, Entity as Log, Model as LogModel},
    log_detail::{self, Column as LogDetailColumn, Entity as LogDetail, Model as LogDetailModel},
    program::{self, Column as ProgramColumn, Entity as Program, Model as ProgramModel},
};
use sea_orm::*;

pub struct Query;

impl Query {
    // region: application
    pub async fn get_application_by_id(
        db: &DbConn,
        id: i32,
    ) -> Result<Option<application::Model>, DbErr> {
        Application::find_by_id(id).one(db).await
    }

    pub async fn get_by_application_id(
        db: &DbConn,
        id: String,
    ) -> Result<Option<application::Model>, DbErr> {
        Application::find()
            .filter(ApplicationColumn::ApplicationId.eq(id))
            .one(db)
            .await
    }

    pub async fn get_applications_in_page(
        db: &DbConn,
        page: u64,
        page_size: u64,
    ) -> Result<(Vec<ApplicationModel>, u64), DbErr> {
        let paginator = Application::find()
            .order_by_asc(ApplicationColumn::Id)
            .paginate(db, page_size);
        let num_pages = paginator.num_pages().await?;

        paginator.fetch_page(page - 1).await.map(|p| (p, num_pages))
    }
    // endregion

    // region: log
    pub async fn get_log_by_id(db: &DbConn, id: String) -> Result<Option<log::Model>, DbErr> {
        Log::find_by_id(id).one(db).await
    }

    pub async fn get_logs_in_page(
        db: &DbConn,
        page: u64,
        page_size: u64,
    ) -> Result<(Vec<LogModel>, u64), DbErr> {
        let paginator = Log::find()
            .order_by_asc(LogColumn::Id)
            .paginate(db, page_size);
        let num_pages = paginator.num_pages().await?;

        paginator.fetch_page(page - 1).await.map(|p| (p, num_pages))
    }
    // endregion

    // region: log_detail
    pub async fn get_log_detail_by_id(
        db: &DbConn,
        id: String,
    ) -> Result<Option<log_detail::Model>, DbErr> {
        LogDetail::find_by_id(id).one(db).await
    }

    pub async fn get_log_details_in_page(
        db: &DbConn,
        page: u64,
        page_szie: u64,
    ) -> Result<(Vec<LogDetailModel>, u64), DbErr> {
        let paginator = LogDetail::find()
            .order_by_asc(LogDetailColumn::Id)
            .paginate(db, page_szie);
        let num_pages = paginator.num_pages().await?;

        paginator.fetch_page(page - 1).await.map(|p| (p, num_pages))
    }
    // endregion

    // region: program
    pub async fn get_program_by_id(
        db: &DbConn,
        id: String,
    ) -> Result<Option<program::Model>, DbErr> {
        Program::find_by_id(id).one(db).await
    }

    pub async fn get_programs_in_page(
        db: &DbConn,
        page: u64,
        page_szie: u64,
    ) -> Result<(Vec<ProgramModel>, u64), DbErr> {
        let paginator = Program::find()
            .order_by_asc(ProgramColumn::Id)
            .paginate(db, page_szie);
        let num_pages = paginator.num_pages().await?;

        paginator.fetch_page(page - 1).await.map(|p| (p, num_pages))
    }
    // endregion
}
