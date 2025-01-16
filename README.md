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