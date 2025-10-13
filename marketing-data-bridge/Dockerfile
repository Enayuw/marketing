#指定基础镜像
FROM registry.100credit.cn/centos7/jdk17:v1.0
#Dockerfile使用变量（可以在Dockerfile中进行引用）
ARG APPNAME=marketing-data-bridge
ARG APP_HOME=/opt/SpringCloud
#工作目录
WORKDIR $APP_HOME
#指定appname标签
LABEL appname=$APPNAME \
#指定version标签
version=1.0.0
#把在宿主机上编译好的jar包复制到容器内的指定地址，格式为”COPY [宿主机目录] [容器内目录]“，注意宿主机目录一定是Dockerfile所在目录的相对目录
COPY $APPNAME/target/marketing-data-bridge*.jar $APP_HOME/app/$APPNAME/lib/
#拷贝不同环境配置文件到config目录
COPY $APPNAME/src/main/resources/ $APP_HOME/app/$APPNAME/config/
#拷贝启动文件到 工作目录
COPY $APPNAME/cloudserver.sh $APP_HOME
#环境变量配置
#0为注册中心，1为非注册中心应用
ENV APP_TYPE=1 \
#HOME目录
APP_HOME=$APP_HOME \
#服务目录
SERVICE_HOME=$APP_HOME/app/$APPNAME \
#启动类
MAIN_CLASS=com.br.marketing.bridge.DataBridgeApplication \
#application文件名
APP_CONFIG=application.yml \
#是否依赖公共jar, 1 依赖 2 不依赖
DEPEND_COMMON=2 \
#JVM参数
JAVA_OPTS="-Xms2458M -Xmx2458M -Xss256K -XX:MetaspaceSize=256M -XX:MaxMetaspaceSize=256M -XX:+UseG1GC -XX:+AlwaysPreTouch -XX:-ResizePLAB -XX:+ParallelRefProcEnabled -XX:+ExplicitGCInvokesConcurrent -XX:+UnlockExperimentalVMOptions -XX:+PrintTenuringDistribution -XX:+PrintAdaptiveSizePolicy -XX:+PrintCommandLineFlags -XX:+PrintReferenceGC -XX:ParallelGCThreads=12 -XX:MaxGCPauseMillis=200 -XX:GCTimeRatio=19 -XX:G1HeapRegionSize=8m -XX:G1RSetUpdatingPauseTimePercent=5 -XX:MaxTenuringThreshold=5 -XX:G1NewSizePercent=20 -XX:G1MaxNewSizePercent=50 -XX:InitiatingHeapOccupancyPercent=45 --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.io=ALL-UNNAMED --add-opens java.base/java.math=ALL-UNNAMED --add-opens java.base/java.net=ALL-UNNAMED --add-opens java.base/java.nio=ALL-UNNAMED --add-opens java.base/java.security=ALL-UNNAMED --add-opens java.base/java.text=ALL-UNNAMED --add-opens java.base/java.time=ALL-UNNAMED --add-opens java.base/java.util=ALL-UNNAMED --add-opens java.base/jdk.internal.access=ALL-UNNAMED --add-opens java.base/jdk.internal.misc=ALL-UNNAMED --add-opens java.base/java.lang.reflect=ALL-UNNAMED -Duser.timezone=Asia/Shanghai -Dbr.grpc.server.bucket=0.005,0.01,0.015,0.02,0.05,0.1,0.2,0.3,0.5 -Dbr.grpc.client.bucket=0.001,0.005,0.01,0.05,0.075,0.1,0.25,0.5,1.0,2.0,5.0,10.0" \
#GC日志参数
JVM_LOG="-verbose:gc -Xlog:async -Xlog:gc*,safepoint:file=gc.log.current:t,u,hn,p,ti,l,tg:filecount=5,filesize=40m" \
#GC日志文件
LOG_FILE="gc.log.current" \
#启动参数(保留用于向后兼容)
APP_PARAM=""
#指定启动容器时执行的命令
CMD ["sh", "cloudserver.sh", "start"]