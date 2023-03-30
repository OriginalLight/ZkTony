fn main() {
    tonic_build::configure()
        .build_server(true)
        .compile(&["application.proto"], &["./proto"])
        .unwrap_or_else(|e| panic!("Failed to compile proto(s) {:?}", e));

    tonic_build::configure()
        .build_server(true)
        .compile(&["log.proto"], &["./proto"])
        .unwrap_or_else(|e| panic!("Failed to compile proto(s) {:?}", e));

    tonic_build::configure()
        .build_server(true)
        .compile(&["program.proto"], &["./proto"])
        .unwrap_or_else(|e| panic!("Failed to compile proto(s) {:?}", e));

    tonic_build::configure()
        .build_server(true)
        .compile(&["log_detail.proto"], &["./proto"])
        .unwrap_or_else(|e| panic!("Failed to compile proto(s) {:?}", e));
}
