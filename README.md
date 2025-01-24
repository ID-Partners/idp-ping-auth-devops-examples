# idp-ping-auth-devops-examples
Repo holding a worked example of a dockerised ping authorize solution using common configs for enterprise deployments


## How to run

1. Register for Ping devops at https://support.pingidentity.com/ Get your devops user id and key via email from ping. 
2. Update the docker-compose.yaml file with these details for both ping authorize and ping authorize pap.
3. Run `docker-compose up --build` to start the containers.
4. Go to https://localhost:7443 for the policy editor and https://localhost:8443 for the authorize admin console.
5. Load AuthZen snapshot into the Policy Editor for a working example of a policy set with the /governance-engine from ./config/AuthZEN_1.0.snapshot
6. For the sideband API use the Common_v1.0.snapshot. Load this int the PAP Editor. 

Remember to check that the dsconfigs are set for both the governance API and the sideband API. 

You will only be able to run against one policy branch and node at a time. Make these config change in the docker compose file.

AuthZEN Branch: 
```
      - POLICY_NODE_ID=c13a3cf0-779b-4805-8738-cf63e22bddbc
      - POLICY_BRANCH=AuthZEN
```

Common Branch:
```
      - POLICY_NODE_ID=c13a3cf0-779b-4805-8738-cf63e22bddbc
      - POLICY_BRANCH=Common
```

## Launch Darkly

Launch Darkly is used to control the feature flags for the application. You will need to create a new project in Launch Darkly get the SDK Key and update the docker-compose file with the SDK key.

See the readme in the launch darkly folder for more details.

# Testing the polcies
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

