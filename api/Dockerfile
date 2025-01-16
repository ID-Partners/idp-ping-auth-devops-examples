# Stage 1: Build the Go application
FROM golang:1.22.5 AS builder

# Set the working directory
WORKDIR /app

# Copy Go modules and install dependencies
COPY go.mod go.sum ./
RUN go mod download

# Copy the rest of the application source code
COPY . .

# Build the application binary for Linux
RUN CGO_ENABLED=0 GOOS=linux GOARCH=amd64 go build -o main .

# Stage 2: Create the final lightweight image
FROM alpine:latest
RUN apk --no-cache add ca-certificates

# Set the working directory
WORKDIR /app

# Copy the compiled binary from the builder stage
COPY --from=builder /app/main .

# Ensure the binary is executable
RUN chmod +x main

# Expose the application's port
EXPOSE 8081

# Run the application
CMD ["./main"]
