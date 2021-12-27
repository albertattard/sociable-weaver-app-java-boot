FROM ubuntu:22.04 AS assembler
MAINTAINER Albert Attard "albertattard@gmail.com"
ENV DEBIAN_FRONTEND=noninteractive
RUN apt-get update \
 && apt-get -y install openjdk-17-jdk \
 && apt-get clean
WORKDIR /opt/app
COPY ./gradle gradle
COPY gradlew gradlew
COPY settings.gradle settings.gradle
RUN ./gradlew
COPY build.gradle build.gradle
COPY gradle.properties gradle.properties
COPY lombok.config lombok.config
COPY ./src src
RUN ./gradlew assemble

FROM ubuntu:22.04 AS layertools
MAINTAINER Albert Attard "albertattard@gmail.com"
ENV DEBIAN_FRONTEND=noninteractive
RUN apt-get update \
 && apt-get -y install openjdk-17-jdk \
 && apt-get clean
WORKDIR /opt/app
COPY --from=assembler /opt/app/build/libs/sociable-weaver-app-java-boot.jar application.jar
RUN java -Djarmode=layertools -jar application.jar extract

FROM ubuntu:22.04
WORKDIR /opt/app
MAINTAINER Albert Attard "albertattard@gmail.com"
ENV DEBIAN_FRONTEND=noninteractive
RUN apt-get update \
 && echo 'tzdata tzdata/Areas select Europe' | debconf-set-selections \
 && echo 'tzdata tzdata/Zones/Europe select Berlin' | debconf-set-selections \
 && apt-get install -y tzdata \
 && dpkg-reconfigure --frontend noninteractive tzdata \
 && apt-get install -y openjdk-17-jdk \
 && apt-get install -y curl \
# && apt-get install -y git \
# && apt-get install -y tree \
 && apt-get install -y zip \
# && apt-get install -y unzip \
# && apt-get install -y vim \
 && apt-get clean
WORKDIR /opt/app
COPY --from=layertools /opt/app/dependencies ./
COPY --from=layertools /opt/app/spring-boot-loader ./
COPY --from=layertools /opt/app/snapshot-dependencies ./
COPY --from=layertools /opt/app/application ./
EXPOSE 8077
ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]
