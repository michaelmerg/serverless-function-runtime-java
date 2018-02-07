# -- Build Runtime --
FROM openjdk:8-jdk-alpine

WORKDIR /root/

COPY gradle gradle
COPY gradlew gradlew
COPY src src
COPY build.gradle .

RUN mkdir -p ~/.gradle && echo "org.gradle.daemon=false" >> ~/.gradle/gradle.properties && \
	./gradlew --no-daemon build

# -- Build Container --
FROM openjdk:8-jre-alpine
RUN apk --no-cache add ca-certificates
WORKDIR /root/
COPY --from=0 /root/build/libs/* ./
CMD ["java", "-cp", ".:*:./function", "serverless.function.ServerlessFunctionRuntime"]