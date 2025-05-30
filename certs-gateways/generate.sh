#!/bin/bash

set -e

echo "🚀 Setting up mTLS for Kong Gateway and Mocpdp Gateway"

# Step 1: Create the certs directory
mkdir -p certs && cd certs

echo "🔹 Generating Root CA Certificate..."
openssl genrsa -out ca.key 2048
openssl req -x509 -new -nodes -key ca.key -sha256 -days 365 -out ca.crt -subj "/CN=MyRootCA"

echo "🔹 Generating Kong's Server Certificate..."
openssl genrsa -out kong.key 2048
openssl req -new -key kong.key -out kong.csr -subj "/CN=kong.example.com"
openssl x509 -req -in kong.csr -CA ca.crt -CAkey ca.key -CAcreateserial -out kong.crt -days 365 -sha256 -extfile <(printf "extendedKeyUsage = serverAuth")

echo "🔹 Generating Mocpdp's Server Certificate..."
openssl genrsa -out mocpdp.key 2048
openssl req -new -key mocpdp.key -out mocpdp.csr -subj "/CN=mocpdp.example.com"
openssl x509 -req -in mocpdp.csr -CA ca.crt -CAkey ca.key -CAcreateserial -out mocpdp.crt -days 365 -sha256 -extfile <(printf "extendedKeyUsage = serverAuth")

echo "🔹 Generating Client Certificate..."
openssl genrsa -out client.key 2048
openssl req -new -key client.key -out client.csr -subj "/CN=trusted-client"
openssl x509 -req -in client.csr -CA ca.crt -CAkey ca.key -CAcreateserial -out client.crt -days 365 -sha256 -extfile <(printf "extendedKeyUsage = clientAuth")

echo "✅ Certificates Generated Successfully!"

# Move back to the project root
cd ..

# Step 2: Create Docker Compose File
echo "📝 Creating Docker Compose Configuration..."
cat > docker-compose.yml <<EOF
version: "3.8"

services:
  kong:
    image: kong/kong-gateway:latest
    container_name: kong
    restart: always
    ports:
      - "8000:8000"   # Proxy (HTTP)
      - "8443:8443"   # Proxy (HTTPS with mTLS)
    environment:
      KONG_DATABASE: "off"
      KONG_DECLARATIVE_CONFIG: "/etc/kong/kong.yaml"
      KONG_PROXY_LISTEN: "0.0.0.0:8443 ssl"
      KONG_SSL_CERT: "/etc/kong/certs/kong.crt"
      KONG_SSL_CERT_KEY: "/etc/kong/certs/kong.key"
      KONG_SSL_CLIENT_CERTIFICATE: "/etc/kong/certs/ca.crt"
      KONG_NGINX_PROXY_SSL_VERIFY_CLIENT: "on"
    volumes:
      - ./certs:/etc/kong/certs

  mocpdp:
    image: nginx:latest
    container_name: mocpdp
    restart: always
    ports:
      - "9443:9443"
    volumes:
      - ./certs:/etc/mocpdp/certs
      - ./mocpdp.conf:/etc/nginx/nginx.conf
EOF

echo "📝 Creating Mocpdp Nginx Configuration..."
cat > mocpdp.conf <<EOF
server {
    listen 9443 ssl;
    ssl_certificate /etc/mocpdp/certs/mocpdp.crt;
    ssl_certificate_key /etc/mocpdp/certs/mocpdp.key;

    # Require Kong to present a valid certificate
    ssl_verify_client on;
    ssl_client_certificate /etc/mocpdp/certs/ca.crt;
    ssl_verify_depth 2;

    location / {
        proxy_pass http://upstream_service;
    }
}
EOF

echo "🚀 Starting Kong and Mocpdp..."
docker compose up -d

echo "✅ Setup Complete!"