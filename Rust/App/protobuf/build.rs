fn main() {
    tonic_build::configure()
        .build_client(false)
        .build_server(true)
        .compile(
            &[
                "application.proto",
                "log.proto",
                "program.proto",
                "log_detail.proto",
                "test.proto",
            ],
            &["./proto"],
        )
        .unwrap_or_else(|e| panic!("Failed to compile proto(s) {:?}", e));
}
