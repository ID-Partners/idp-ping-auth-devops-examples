FROM golang:1.21-alpine AS builder
WORKDIR /src
COPY . .
RUN go build -o bank-api

FROM alpine:latest
WORKDIR /app
COPY --from=builder /src/bank-api .
EXPOSE 8445
ENTRYPOINT ["./bank-api"]