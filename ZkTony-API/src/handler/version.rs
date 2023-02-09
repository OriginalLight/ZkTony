use axum::{extract::Path, Extension};
use sea_orm::EntityTrait;
use std::sync::Arc;

use super::get_conn;
use crate::{entity::version, res::Res, state::AppState};

pub async fn get_by_id(
    Extension(state): Extension<Arc<AppState>>,
    Path(id): Path<i32>,
) -> Res<version::Model> {
    let conn = get_conn(&state);
    let res = version::Entity::find_by_id(id).one(conn).await;
    match res {
        Ok(v) => Res::with_data(v.unwrap()),
        Err(e) => Res::with_err(&e.to_string()),
    }
}
