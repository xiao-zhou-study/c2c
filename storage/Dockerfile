FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone
RUN addgroup -g 1000 app && adduser -u 1000 -G app -s /bin/sh -D app
COPY target/*.jar app.jar
ENV JAVA_OPTS="-Xms128m -Xmx256m -Duser.timezone=Asia/Shanghai"
EXPOSE 8086
USER app
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
