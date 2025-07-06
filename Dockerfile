# 使用OpenJDK 17作为基础镜像
FROM openjdk:17-jre-slim

# 设置工作目录
WORKDIR /app

# 设置环境变量
ENV JAVA_OPTS="-Xms512m -Xmx2g"

# 创建非root用户
RUN groupadd -r spider && useradd -r -g spider spider

# 复制JAR文件
COPY target/spider-evaluation-1.0.0.jar app.jar

# 创建数据目录
RUN mkdir -p /app/data /app/results && \
    chown -R spider:spider /app

# 切换到非root用户
USER spider

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD java -version || exit 1

# 暴露端口（如果将来添加Web界面）
EXPOSE 8080

# 设置入口点
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar \"$@\"", "--"]

# 默认命令显示帮助信息
CMD ["--help"] 