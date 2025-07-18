FROM eclipse-temurin:17-jdk

USER root

# Install docker client, git, openjdk 17
RUN apt-get update && \
    apt-get install -y docker.io git openjdk-17-jdk && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
ENV PATH="$JAVA_HOME/bin:$PATH"
