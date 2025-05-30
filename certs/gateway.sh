#!/bin/bash

# Set names for the CA and Gateway certificates
CA_KEY="ca.key"
CA_CERT="ca.crt"
CA_PEM="ca.pem"

GATEWAY_KEY="gateway.key"
GATEWAY_CSR="gateway.csr"
GATEWAY_CERT="gateway.crt"
GATEWAY_PEM="gateway.pem"

DAYS_VALID=365  # Certificate validity period

echo "🔹 Generating a new Certificate Authority (CA)..."
openssl req -new -newkey rsa:4096 -nodes -keyout ca.key -x509 -days 365 -out ca.crt \
  -subj "/CN=My Root CA" \
  -addext "basicConstraints=CA:TRUE" \
  -addext "keyUsage=keyCertSign, cRLSign" \
  -addext "subjectKeyIdentifier=hash" \
  -addext "authorityKeyIdentifier=keyid:always"

echo "🔹 Converting CA certificate to PEM format..."
openssl x509 -in $CA_CERT -out $CA_PEM -outform PEM

echo "✅ CA certificate and key generated: $CA_CERT, $CA_KEY"

# Generate key and CSR for Kong Gateway
echo "🔹 Generating a private key and certificate signing request (CSR) for Kong Gateway..."
openssl req -new -newkey rsa:2048 -nodes -keyout $GATEWAY_KEY -out $GATEWAY_CSR \
  -subj "/CN=kong-gateway" \
  -addext "subjectAltName=DNS:kong-gateway,DNS:kong-gateway.local"

# Sign the Gateway certificate with the CA
echo "🔹 Signing the Kong Gateway certificate with the CA..."
openssl x509 -req -in $GATEWAY_CSR -CA $CA_CERT -CAkey $CA_KEY -CAcreateserial -out $GATEWAY_CERT -days $DAYS_VALID

# Create a PEM bundle for the Gateway
echo "🔹 Creating a PEM bundle for Kong Gateway..."
cat $GATEWAY_KEY $GATEWAY_CERT > $GATEWAY_PEM

# Compute the SHA256 fingerprint of the CA certificate
SHA256_FINGERPRINT=$(openssl x509 -in $CA_PEM -noout -fingerprint -sha256 | tr -d ':')
echo "✅ SHA256 fingerprint of CA certificate: $SHA256_FINGERPRINT"

# Output file names
echo -e "\n🔹 Generated files:"
echo "  - CA Key: $CA_KEY"
echo "  - CA Certificate: $CA_CERT"
echo "  - CA PEM: $CA_PEM"
echo "  - Kong Gateway Key: $GATEWAY_KEY"
echo "  - Kong Gateway Certificate: $GATEWAY_CERT"
echo "  - Kong Gateway PEM: $GATEWAY_PEM"

echo "🎉 Certificates and keys successfully created!"