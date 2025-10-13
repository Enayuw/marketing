# 指定基础镜像
FROM registry.100credit.cn/base/jdk:openjdk-17.0.10_7

# Dockerfile使用变量
ARG APPNAME=marketing-data-bridge
ARG APP_HOME=/opt/SpringCloud

# 工作目录
WORKDIR $APP_HOME

# 指定标签
LABEL appname=$APPNAME \
version=1.0.0

# 复制jar包到容器
COPY $APPNAME/target/marketing-data-bridge*.jar $APP_HOME/app/$APPNAME/lib/

# 拷贝配置文件到config目录
COPY $APPNAME/src/main/resources/ $APP_HOME/app/$APPNAME/config/

# 环境变量配置
ENV APP_HOME=$APP_HOME \
APPNAME=$APPNAME \
TZ=Asia/Shanghai \
# JVM参数
JAVA_OPTS="-Xms2458M -Xmx2458M -Xss256K -XX:MaxDirectMemorySize=2458M -XX:MetaspaceSize=128M -XX:MaxMetaspaceSize=256M -XX:+UseG1GC -XX:+AlwaysPreTouch -XX:+ParallelRefProcEnabled -XX:+UnlockExperimentalVMOptions -XX:ParallelGCThreads=12 -XX:MaxGCPauseMillis=20 -XX:G1HeapRegionSize=8m -XX:MaxTenuringThreshold=5 -XX:G1NewSizePercent=15 -XX:G1MaxNewSizePercent=50 -XX:InitiatingHeapOccupancyPercent=35 -XX:+UseStringDeduplication -XX:+HeapDumpOnOutOfMemoryError --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.io=ALL-UNNAMED --add-opens java.base/java.math=ALL-UNNAMED --add-opens java.base/java.net=ALL-UNNAMED --add-opens java.base/java.nio=ALL-UNNAMED --add-opens java.base/java.security=ALL-UNNAMED --add-opens java.base/java.text=ALL-UNNAMED --add-opens java.base/java.time=ALL-UNNAMED --add-opens java.base/java.util=ALL-UNNAMED --add-opens java.base/jdk.internal.access=ALL-UNNAMED --add-opens java.base/jdk.internal.misc=ALL-UNNAMED --add-opens java.base/java.lang.reflect=ALL-UNNAMED --add-opens java.base/jdk.internal.loader=ALL-UNNAMED -Dbr.grpc.server.bucket=0.005,0.01,0.015,0.02,0.05,0.1,0.2,0.3,0.5 -Dbr.grpc.client.bucket=0.001,0.005,0.01,0.05,0.075,0.1,0.25,0.5,1.0,2.0,5.0,10.0 -Duser.timezone=Asia/Shanghai" \
# GC日志参数
JVM_LOG="-Xlog:gc*,safepoint:file" \
# GC日志文件配置
LOG_FILE="gc_%p_%t.log:time,uptime,level,tags:filecount=2,filesize=20m"

# 配置时区
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 启动命令 - 按照架构部门标准格式
CMD ["sh", "-c", "LOG_PATH=${APP_HOME}/logs/${APPNAME}/${POD_NAME} && mkdir -p $LOG_PATH && $JAVA_HOME/bin/java ${JAVA_OPTS} -XX:HeapDumpPath=$LOG_PATH/heapdump.hprof $JVM_LOG=$LOG_PATH/$LOG_FILE -Xbootclasspath/a:$APP_HOME/app/$APPNAME/config/${CONF_ENV} -jar $APP_HOME/app/$APPNAME/lib/${APPNAME}.jar"]