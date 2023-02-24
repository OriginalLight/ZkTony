# NB: This is not a production-grade Dockerfile.

#################
## build stage ##
#################
FROM rust AS builder
WORKDIR /code

# Download crates-io index and fetch dependency code.
# This step avoids needing to spend time on every build downloading the index
# which can take a long time within the docker context. Docker will cache it.
RUN USER=root cargo init
COPY ./Cargo.toml Cargo.toml
COPY ./.env .env
COPY ./src src
COPY ./api api
COPY ./core core
COPY ./entity entity

# compile app
RUN cargo build --release

###############
## run stage ##
###############
FROM debian:bullseye-slim
WORKDIR /app

# copy server binary from build stage
COPY --from=builder /code/target/release/zktony zktony
COPY --from=builder /code/.env /.env

# indicate what port the server is running on
EXPOSE 9765

# run server
CMD [ "/app/zktony" ]