# NB: This is not a production-grade Dockerfile.

#################
## build stage ##
#################
FROM rust:1-slim-bullseye AS builder
WORKDIR /code

# install system dependencies
RUN apt-get update && apt-get install -y libssl-dev pkg-config

# Download crates-io index and fetch dependency code.
# This step avoids needing to spend time on every build downloading the index
# which can take a long time within the docker context. Docker will cache it.
RUN USER=root cargo init
COPY Cargo.toml Cargo.toml
COPY config.toml config.toml
COPY api api
COPY bin bin
COPY configs configs
COPY db db
COPY service service
COPY utils utils

# compile app
RUN cargo build --release

###############
## run stage ##
###############
FROM debian:bullseye-slim
WORKDIR /app

# copy server binary from build stage
COPY --from=builder /code/target/release/zktony-api zktony-api
COPY --from=builder /code/config.toml config.toml

# indicate what port the server is running on
EXPOSE 9765

# run server
CMD [ "/app/zktony-api" ]