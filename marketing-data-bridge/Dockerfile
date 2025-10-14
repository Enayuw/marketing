# 指定基础镜像
FROM registry.100credit.cn/base/jdk:openjdk-17.0.10_7

# 工作目录
WORKDIR /opt/SpringCloud

# 指定标签
LABEL appname=marketing-data-bridge \
      version=1.0.0

# 创建目录结构
RUN mkdir -p /opt/SpringCloud/app/marketing-data-bridge/lib && \
    mkdir -p /opt/SpringCloud/app/marketing-data-bridge/config

# 复制jar包到容器并重命名为标准名称
COPY marketing-data-bridge/target/marketing-data-bridge*.jar /opt/SpringCloud/app/marketing-data-bridge/lib/marketing-data-bridge.jar

# 拷贝配置文件到config目录
COPY marketing-data-bridge/src/main/resources/ /opt/SpringCloud/app/marketing-data-bridge/config/

# 环境变量配置
ENV APP_HOME=/opt/SpringCloud \
    TZ=Asia/Shanghai

# 配置时区
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 启动命令 - 由K8s的command覆盖
CMD ["sh", "-c", "echo 'Container ready. Waiting for K8s command...'"]