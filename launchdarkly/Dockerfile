FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

# Install Maven manually
RUN apt-get update && \
    apt-get install -y maven && \
    rm -rf /var/lib/apt/lists/*

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src

RUN mvn clean test

RUN mvn package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /app/target/launchdarkly-sidecar-1.0.0.jar sidecar.jar

EXPOSE 8081
ENV LAUNCHDARKLY_SDK_KEY="sdk-0b9b2fb6-e74e-46f8-8a75-eab8a0e9055f"
ENTRYPOINT ["java", "-jar", "sidecar.jar"]