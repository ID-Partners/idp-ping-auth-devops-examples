FROM kong/kong-gateway:3.4

USER root

# Install Git so we can clone the fork
RUN apt-get update \
 && apt-get install -y --no-install-recommends \
      git \
      unzip \
      curl \
 && rm -rf /var/lib/apt/lists/*


USER root
