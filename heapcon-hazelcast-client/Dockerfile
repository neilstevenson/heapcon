FROM openjdk:8-jre

# Copy from Maven build output
ARG JAR_FILE
ADD target/${JAR_FILE} application.jar

# Environment variable, default to empty string
ENV KUBERNETES ""

ENTRYPOINT exec java -Dkubernetes=$KUBERNETES -jar application.jar
