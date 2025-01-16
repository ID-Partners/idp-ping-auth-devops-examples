#!/bin/sh

# Wait for Kong Admin API to be up
until curl -s http://localhost:8001; do
  echo "Waiting for Kong Admin API..."
  sleep 2
done

# Add services
curl -i -X POST http://localhost:8001/services/ --data name=go-api --data url='http://go-app:8081'

# Add routes
curl -i -X POST http://localhost:8001/services/go-api/routes \
      --data "paths[]=/" \
     --data "methods[]=POST"

# Add plugins
curl -i -X POST http://localhost:8001/services/go-api/plugins \
     --data "name=ping-auth" \
     --data "config.service_url=https://ping-authorize:1443/" \
     --data "config.shared_secret=2FederateM0re" \
     --data "config.secret_header_name=CLIENT_TOKEN" \
     --data "config.verify_service_certificate=false" \
     --data "config.enable_debug_logging=true"

echo "Kong Configuration Complete!"