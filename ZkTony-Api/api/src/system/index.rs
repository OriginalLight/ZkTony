use axum::response::Html;

pub async fn index() -> Html<&'static str> {
    Html("<h1>ZkTony API</h1>")
}
