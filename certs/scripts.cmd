openssl req -new -newkey rsa:2048 -nodes -keyout pap.key -out pap.csr  -subj "/CN=ping-authorize-pap" -addext "subjectAltName=DNS:ping-authorize-pap,DNS:ping-authorize-pap.external"

openssl req -new -newkey rsa:2048 -nodes -keyout pdp.key -out pdp.csr  -subj "/CN=ping-authorize-pdp"   -addext "subjectAltName=DNS:ping-authorize-pdp,DNS:ping-authorize-pdp.external"

openssl x509 -req -in pap.csr -signkey pap.key -days 365 -out pap.crt

openssl x509 -req -in pdp.csr -signkey pdp.key -days 365 -out pdp.crt

openssl pkcs12 -export -inkey pap.key -in pap.crt -out pap.p12 -name pap   -passout pass:changeit

openssl pkcs12 -export -inkey pdp.key -in pdp.crt -out pdp.p12 -name pdp   -passout pass:changeit

keytool -importkeystore -srckeystore pap.p12 -srcstoretype pkcs12 -srcstorepass changeit   -destkeystore pap.jks -deststoretype jks -deststorepass changeit

keytool -importkeystore  -srckeystore pdp.p12 -srcstoretype pkcs12 -srcstorepass changeit -destkeystore pdp.jks -deststoretype jks -deststorepass changeit


  keytool -import -trustcacerts -alias pap -file pap.crt -keystore pdp.jks -storepass changeit

  docker exec -it  authZEN-pdp openssl s_client -connect  ping-authorize-pap:1443 -showcerts

  openssl s_client -connect localhost:7443 -showcerts