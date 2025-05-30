openssl req -new -newkey rsa:2048 -nodes -keyout gateway.key -out gateway.csr  -subj "/CN=gateway" -addext "subjectAltName=DNS:gateway,DNS:gateway.external"

openssl x509 -req -in pap.csr -signkey gateway.key -days 365 -out gateway.crt

openssl pkcs12 -export -inkey gateway.key -in gateway.crt -out gateway.p12 -name gateway 
