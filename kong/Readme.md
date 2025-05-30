



# Kong Manual Config Scripts

This project uses a static configuration file for Kong.

Use configure-kong.sh to configure Kong is using a database.

docker compose run --rm kong kong config db_import /kong/declarative/kong.yaml

curl -v -k \
  --cert ./gateway.crt \
  --key ./gateway.key \
  --cacert ./ca.pem \
  --location --request POST https://localhost:9443/limits


  curl -v -k --location --request POST 'https://localhost:9443/limits'