# Default app name
ARG APPNAME=marketing-data-bridge
# Default working directory
ARG APP_HOME=/opt/SpringCloud
# The default JAR package name
ARG APPLICATION_NAME=marketing-data-bridge

# In the first stage, the build is divided into applications
# Base image
FROM registry.100credit.cn/base/jdk:openjdk-17.0.10_7 as builder

ARG APPNAME
ARG APP_HOME
# The default JAR package name
ARG APPLICATION_NAME
#
ARG JAR_FILE=${APPLICATION_NAME}/target/${APPLICATION_NAME}*.jar

# Working directory
WORKDIR ${APP_HOME}

# Copy the application package
COPY ${JAR_FILE} ${APPLICATION_NAME}.jar

# Extract layering
RUN java -Djarmode=tools -jar ${APPLICATION_NAME}.jar extract --layers --launcher

# The second stage is to build a hierarchical image
# Base image
FROM registry.100credit.cn/base/jdk:openjdk-17.0.10_7

ARG APPNAME
ARG APP_HOME
ARG APPLICATION_NAME

# Working directory
WORKDIR $APP_HOME

# Specify the appname tag
LABEL appname=$APPNAME \
version=1.0.0

# Copy the boot file to the working directory
COPY --from=builder $APP_HOME/${APPLICATION_NAME}/dependencies/ ./
COPY --from=builder $APP_HOME/${APPLICATION_NAME}/spring-boot-loader/ ./
COPY --from=builder $APP_HOME/${APPLICATION_NAME}/snapshot-dependencies/ ./
COPY --from=builder $APP_HOME/${APPLICATION_NAME}/application/ ./

# Time zone setting
ENV TZ="Asia/Shanghai" \
# HOME DIRECTORY
APP_HOME=$APP_HOME \
# environment
CONF_ENV="prod" \
# configuration
PROFILES_NAME="" \
# Whether or not to depend on the public jar, 1 depends on 2 does not depend
DEPEND_COMMON=2 \
# Optimize the configuration
JAVA_OPTS="-Xms4G -Xmx4G -XX:MaxDirectMemorySize=4G -XX:MetaspaceSize=256M -XX:MaxMetaspaceSize=256M -XX:+UseG1GC -XX:+AlwaysPreTouch -XX:-ResizePLAB -XX:+ParallelRefProcEnabled -XX:+ExplicitGCInvokesConcurrent -XX:MaxGCPauseMillis=200 -XX:+UseStringDeduplication -XX:+UnlockExperimentalVMOptions -XX:G1NewSizePercent=20 -XX:InitiatingHeapOccupancyPercent=45 --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.io=ALL-UNNAMED --add-opens java.base/java.math=ALL-UNNAMED --add-opens java.base/java.net=ALL-UNNAMED --add-opens java.base/java.nio=ALL-UNNAMED --add-opens java.base/java.security=ALL-UNNAMED --add-opens java.base/java.text=ALL-UNNAMED --add-opens java.base/java.time=ALL-UNNAMED --add-opens java.base/java.util=ALL-UNNAMED --add-opens java.base/jdk.internal.access=ALL-UNNAMED --add-opens java.base/jdk.internal.misc=ALL-UNNAMED --add-opens java.base/java.lang.reflect=ALL-UNNAMED --add-opens java.base/jdk.internal.loader=ALL-UNNAMED -Dbr.grpc.server.bucket=0.005,0.01,0.015,0.02,0.05,0.1,0.2,0.3,0.5 -Dbr.grpc.client.bucket=0.001,0.005,0.01,0.05,0.075,0.1,0.25,0.5,1.0,2.0,5.0,10.0 -Duser.timezone=Asia/Shanghai" \
# Virtual machine GC configuration
JVM_LOG="-verbose:gc -Xlog:async -Xlog:safepoint,classhisto*=trace,age*,gc*=info:file" \
# GC log file
LOG_FILE="gc_%p_%t.log:t,um,hn,p,ti,l,tg:filecount=10,filesize=50m "

# Soft chain time zone
RUN ln -snf /usr/share/zoneinfo/${TZ} /etc/localtime && echo ${TZ} > /etc/timezone

# Default portal command
ENTRYPOINT ["sh","-c","CONF_PATH=${APP_HOME}/BOOT-INF/classes/${PROFILES_NAME:+${PROFILES_NAME}/}${CONF_ENV} && LOG_PATH=${APP_HOME}/logs/${APPNAME}/${POD_NAME} && mkdir -p $LOG_PATH && java ${JAVA_OPTS} $JVM_LOG=$LOG_PATH/${LOG_FILE} -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=${LOG_PATH}/heapdump.hprof -Xbootclasspath/a:${CONF_PATH} org.springframework.boot.loader.launch.JarLauncher"]