fn main() {
    tonic_build::configure()
        .build_client(false)
        .build_server(true)
        .compile(
            &[
                "customer.proto",
                "instrument.proto",
                "order.proto",
                "software.proto",
            ],
            &["./proto"],
        )
        .unwrap_or_else(|e| panic!("Failed to compile proto(s) {:?}", e));
}
