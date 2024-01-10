// Prevents additional console window on Windows in release, DO NOT REMOVE!!
#![cfg_attr(not(debug_assertions), windows_subsystem = "windows")]

// Learn more about Tauri commands at https://tauri.app/v1/guides/features/command
#[tauri::command]
fn greet(name: &str) -> String {
    format!("Hello, {}! You've been greeted from Rust!", name)
}

// 获取全部串口名
#[tauri::command]
fn serialports() -> Vec<String> {
    let ports = serialport::available_ports().expect("No ports found!");
    return ports.iter().map(|p| p.port_name.to_owned()).collect();
}

fn main() {
    tauri::Builder::default()
        .invoke_handler(tauri::generate_handler![greet, serialports])
        .run(tauri::generate_context!())
        .expect("error while running tauri application");
}
