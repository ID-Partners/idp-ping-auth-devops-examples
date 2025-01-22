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