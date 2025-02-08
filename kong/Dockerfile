FROM kong/kong-gateway:3.4

USER root

# Install Git so we can clone the fork
RUN apt-get update && \
    apt-get install -y --no-install-recommends git unzip && \
    rm -rf /var/lib/apt/lists/*
    
RUN luarocks install kong-plugin-ping-auth

# Clone the forked plugin
RUN git clone https://github.com/ID-Partners-AU/kong-plugin-ping-auth.git /tmp/kong-plugin-ping-auth

# Create a directory for the custom plugin inside Kong's Lua path
# The plugin folder name should match your plugin's name (in 'handler.lua')
RUN mkdir -p /usr/local/share/lua/5.1/kong/plugins/idp-ping-auth

# Copy the plugin source files into Kong’s plugins directory
RUN cp -r /tmp/kong-plugin-ping-auth/ping-auth/* \
          /usr/local/share/lua/5.1/kong/plugins/idp-ping-auth

# Tell Kong to load both the default (bundled) plugins and your custom plugin
# ENV KONG_PLUGINS="bundled,idp-ping-auth,ping-auth"

# Copy your declarative configuration
COPY kong.yaml /kong/declarative/kong.yaml

USER root
