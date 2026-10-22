FROM gradle:9.5.1-jdk17-alpine AS builder
WORKDIR /app
COPY . .
RUN gradle installDist --no-daemon

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /app/build/install/server /app
EXPOSE 8080
ENTRYPOINT ["/app/bin/server"]
