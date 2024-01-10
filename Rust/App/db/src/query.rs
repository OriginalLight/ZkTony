use ::entity::{form::*, prelude::*};
use sea_orm::*;

pub struct ApplicationQuery;
pub struct LogQuery;
pub struct LogDetailQuery;
pub struct ProgramQuery;

impl ApplicationQuery {
    pub async fn query(
        db: &DbConn,
        form: ApplicationQueryForm,
    ) -> Result<(Vec<ApplicationModel>, u64), DbErr> {
        let mut find = Application::find();

        if form.id != 0 {
            find = find.filter(ApplicationColumn::Id.eq(form.id));
        }

        if !form.application_id.is_empty() {
            find = find.filter(ApplicationColumn::ApplicationId.eq(form.application_id));
        }

        if !form.build_type.is_empty() {
            find = find.filter(ApplicationColumn::BuildType.eq(form.build_type));
        }

        let paginator = find
            .order_by_asc(ApplicationColumn::Id)
            .paginate(db, form.page_size);

        let num_pages = paginator.num_pages().await?;

        paginator
            .fetch_page(form.page - 1)
            .await
            .map(|p| (p, num_pages))
    }
}

impl LogQuery {
    pub async fn query(db: &DbConn, form: LogQueryForm) -> Result<(Vec<LogModel>, u64), DbErr> {
        let mut find = Log::find();

        if !form.id.is_empty() {
            find = find.filter(LogColumn::Id.eq(form.id));
        }

        if !form.sub_id.is_empty() {
            find = find.filter(LogColumn::SubId.eq(form.sub_id));
        }

        if !form.log_type.is_empty() {
            find = find.filter(LogColumn::LogType.eq(form.log_type));
        }

        let paginator = find
            .order_by_asc(LogColumn::Id)
            .paginate(db, form.page_size);
        let num_pages = paginator.num_pages().await?;

        paginator
            .fetch_page(form.page - 1)
            .await
            .map(|p| (p, num_pages))
    }
}

impl LogDetailQuery {
    pub async fn query(
        db: &DbConn,
        form: LogDetailQueryForm,
    ) -> Result<(Vec<LogDetailModel>, u64), DbErr> {
        let mut find = LogDetail::find();

        if !form.id.is_empty() {
            find = find.filter(LogDetailColumn::Id.eq(form.id));
        }

        let paginator = find
            .order_by_asc(LogDetailColumn::Id)
            .paginate(db, form.page_size);
        let num_pages = paginator.num_pages().await?;

        paginator
            .fetch_page(form.page - 1)
            .await
            .map(|p| (p, num_pages))
    }
}

impl ProgramQuery {
    pub async fn query(
        db: &DbConn,
        form: ProgramQueryForm,
    ) -> Result<(Vec<ProgramModel>, u64), DbErr> {
        let mut find = Program::find();

        if !form.id.is_empty() {
            find = find.filter(ProgramColumn::Id.eq(form.id));
        }

        if !form.name.is_empty() {
            find = find.filter(ProgramColumn::Name.eq(form.name));
        }

        let paginator = find
            .order_by_asc(ProgramColumn::Id)
            .paginate(db, form.page_size);
        let num_pages = paginator.num_pages().await?;

        paginator
            .fetch_page(form.page - 1)
            .await
            .map(|p| (p, num_pages))
    }
}
