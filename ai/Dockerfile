# 1. 将 alpine 换成 jammy (Ubuntu) 或 slim (Debian) 系列，它们内置了 glibc
FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

# 2. Ubuntu/Debian 修改时区的方式与 Alpine 不同
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 3. 修改用户创建命令 (Debian 的 addgroup/adduser 参数与 Alpine 不同)
RUN groupadd -g 1000 app && \
    useradd -u 1000 -g app -m -s /bin/bash app

COPY target/*.jar app.jar

ENV JAVA_OPTS="-Xms256m -Xmx512m -Duser.timezone=Asia/Shanghai"

EXPOSE 8088

# 切换到非 root 用户
USER app

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]