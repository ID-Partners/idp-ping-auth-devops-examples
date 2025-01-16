FROM kong:3.4  

USER root
RUN apt-get update && apt-get install -y curl && apt-get clean
# Update package lists and install needed packages
RUN apt-get update && \
    apt-get install -y --no-install-recommends git build-essential luarocks && \
    rm -rf /var/lib/apt/lists/*

RUN luarocks install kong-plugin-ping-auth

# Return to kong user
USER kong