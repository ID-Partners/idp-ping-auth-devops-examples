# idp-ping-auth-devops-examples
Repo holding a worked example of a dockerised ping authorize solution using common configs for enterprise deployments


## How to run

1. Register for Ping devops at https://support.pingidentity.com/ Get your devops user id and key via email from ping. 
2. Update the docker-compose.yaml file with these details for both ping authorize and ping authorize pap.
3. Run `docker-compose up --build` to start the containers.
4. Go to https://localhost:7443 for the policy editor and https://localhost:8443 for the authorize admin console.
5. Load AuthZen snapshot into the Policy Editor for a working example of a policy set from ./config/AuthZEN_1.0.snapshot


Use the following to test the setup:

```
curl -i -X POST http://localhost:8000/payment \
     --header "Content-Type: application/json" \
     --data '{
         "debtorAccount": "12345",
         "creditorAccount": "67890",
         "amount": 100.50,
         "currency": "USD",
         "paymentDetails": "Test Payment"
     }'
 ```

curl --location 'http://kong:8000/payment' \
--header 'Content-Type: application/json' \
--data '{
    "debtorAccount": "12345",
    "creditorAccount": "67890",
    "amount": 100.5,
    "currency": "USD",
    "paymentDetails": "Test Payment"
}'


 docker compose -f docker-compose.yml -f docker-compose.local.yml up


 **{
    "domain": "",
    "service": "PaymentDemo",
    "identityProvider": "",
    "action": "inbound-POST",
    "attributes": {
        "HttpRequest.RequestBody": "{\"debtorAccount\":\"12345\",\"creditorAccount\":\"67890\",\"amount\":100.5,\"currency\":\"USD\",\"paymentDetails\":\"Test Payment\"}",
        "HttpRequest.CorrelationId": "9780c981-4160-4878-8933-c1d6d7ae2a66",
        "HttpRequest.RequestURI": "http://localhost:8000/payment",
        "HttpRequest.IPAddress": "172.25.0.1",
        "HttpRequest.ResourcePath": "payment",
        "Gateway": "{\"_BasePath\":\"/\",\"_TrailingPath\":\"payment\"}",
        "HttpRequest.RequestHeaders": "{\"content-type\":[\"application/json\"],\"user-agent\":[\"PostmanRuntime/7.43.0\"],\"accept\":[\"*/*\"],\"cache-control\":[\"no-cache\"],\"host\":[\"localhost:8000\"],\"content-length\":[\"146\"],\"postman-token\":[\"4239d925-91d8-495d-af3d-4d8633cbc418\"],\"connection\":[\"keep-alive\"],\"accept-encoding\":[\"gzip, deflate, br\"]}",
        "HttpRequest.ResponseHeaders": "{}"
    }
}
**



# Kong Manual Config Scripts



curl -i -X POST http://localhost:8002/services/ \
     --data "name=pdp-service1" \
     --data "url=https://localhost:8000/request"

     curl -i -X POST http://localhost:8002/services/pdp-service1/routes \
     --data "paths[]=/authzen1"

     curl -i -X POST http://localhost:8002/services/pdp-service1/plugins \
     --data "name=ping-auth" \
     --data "config.secret_header_name=PDG-TOKEN" \
     --data "config.shared_secret=2FederateM0re" \
     --data "config.service_url=https://ping-authorize:1443"

     curl -i http://localhost:8000/authzen1

curl -vIk https://ping-authorize:1443/authzen

curl -vk --location 'https://ping-authorize:1443/authzen' \
--header 'PDG-TOKEN: 2FederateM0re' \
--header 'Content-Type: application/json' \
--data '{
    "domain": "idpartners.authorization_details.account_information",
    "service": "Authorization",
    "action": "authorize",
    "attributes": {
        "idp.account_information.type": "\"account_information\"",
        "idp.account_information.recurringIndicator": "true",
        "UserID": "joe",
        "idp.account_information.access": "{\"accounts\":[],\"balances\":[],\"transactions\":[]}"
    }
}'


curl -i -X POST http://localhost:8002/services/ \
     --data "name=go-api" \
     --data "url=http://go-app:8081"

curl -i -X POST http://localhost:8002/services/go-api/routes \
     --data "paths[]=/" \
     --data "methods[]=POST"

curl -i -X POST http://localhost:8002/services/go-api/plugins \
     --data "name=ping-auth" \
     --data "config.service_url=https://ping-authorize:1443/" \
     --data "config.shared_secret=2FederateM0re" \
     --data "config.secret_header_name=CLIENT-TOKEN" 

curl -i -X PATCH http://localhost:8002/plugins/3f948aa8-85df-459f-b1c8-fb96504b899c \
     --data "config.verify_service_certificate=false" \
     --data "config.enable_debug_logging=true"


# List services
curl -X GET http://localhost:8002/services

# List routes
curl -X GET http://localhost:8002/routes

# List plugins
curl -X GET http://localhost:8002/plugins


curl -i -X POST http://localhost:8000/payment \
     --header "Content-Type: application/json" \
     --data '{
         "debtorAccount": "12345",
         "creditorAccount": "67890",
         "amount": 100.50,
         "currency": "USD",
         "paymentDetails": "Test Payment"
     }'


curl -i -X POST http://localhost:8081/payment \
     --header "Content-Type: application/json" \
     --data '{
         "debtorAccount": "12345",
         "creditorAccount": "67890",
         "amount": 100.50,
         "currency": "USD",
         "paymentDetails": "Test Payment"
     }'

     docker-compose exec kong curl -v POST http://go-app:8081/payment \
     --header "Content-Type: application/json" \
     --data '{
         "debtorAccount": "12345",
         "creditorAccount": "67890",
         "amount": 100.50,
         "currency": "USD",
         "paymentDetails": "Test Payment"
     }'




docker run -d \
  --name kong \
  --restart always \
  -e KONG_DATABASE="off" \
  -e KONG_DECLARATIVE_CONFIG="/kong/declarative/kong.yaml" \
  -e KONG_LOG_LEVEL="debug" \
  -e KONG_PROXY_ACCESS_LOG="/dev/stdout" \
  -e KONG_ADMIN_ACCESS_LOG="/dev/stdout" \
  -e KONG_PROXY_ERROR_LOG="/dev/stderr" \
  -e KONG_ADMIN_ERROR_LOG="/dev/stderr" \
  -e KONG_ADMIN_GUI_URL="http://localhost:8002" \
  -e KONG_ADMIN_LISTEN="0.0.0.0:8001, 0.0.0.0:8444 ssl" \
  -e KONG_PROXY_LISTEN="0.0.0.0:8000, 0.0.0.0:8443 ssl" \
  -e KONG_PLUGINS="bundled,ping-auth" \
  -p 8000:8000 \
  -p 9443:8443 \
  -p 8001:8001 \
  -p 8002:8002 \
  -p 8444:8444 \
  -v "$(pwd)/kong/scripts:/kong-scripts" \
  kong-ping-auth:latest

  docker run -d \
  --name kong \
  --restart always \
  -e KONG_DATABASE="off" \
  -e KONG_DECLARATIVE_CONFIG="/kong/declarative/kong.yaml" \
  -e KONG_PLUGINS="bundled,ping-auth" \
  -p 8000:8000 \
  -p 9443:8443 \
  kong-ping-auth:latest