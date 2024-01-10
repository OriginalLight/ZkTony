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
COPY ./api api
COPY ./config config
COPY ./db db
COPY ./entity entity
COPY ./env env
COPY ./protobuf protobuf
COPY ./src src
COPY ./Cargo.toml Cargo.toml



# compile app
RUN apt update && apt install -y protobuf-compiler libprotobuf-dev
RUN cargo build --release

###############
## run stage ##
###############
FROM debian:bullseye-slim
WORKDIR /app

# copy server binary from build stage
COPY --from=builder /code/target/release/zkty zkty
COPY --from=builder /code/env env

# indicate what port the server is running on
EXPOSE 50051 50052

# run server
CMD [ "/app/zkty" ]