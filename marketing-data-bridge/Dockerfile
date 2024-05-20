#指定基础镜像
FROM registry.100credit.cn/centos6/jdk_8u191:v1.0
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
#启动参数
APP_PARAM="-Xmx2458M -Xms2458M -Xss256K -XX:+AlwaysPreTouch -XX:-ResizePLAB -XX:+ParallelRefProcEnabled -XX:+ExplicitGCInvokesConcurrent -XX:+UnlockDiagnosticVMOptions -XX:ParGCCardsPerStrideChunk=4096 -XX:+UseBiasedLocking -XX:MetaspaceSize=128M -XX:+PrintGCDateStamps -XX:+PrintGCTimeStamps -XX:+PrintGCDetails -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=2 -XX:GCLogFileSize=20M -XX:+PrintTenuringDistribution -XX:+PrintAdaptiveSizePolicy -XX:+PrintCommandLineFlags -XX:+PrintReferenceGC -XX:+UseG1GC -XX:ParallelGCThreads=12 -XX:MaxGCPauseMillis=20 -XX:GCTimeRatio=19 -XX:G1HeapRegionSize=8m -XX:G1RSetUpdatingPauseTimePercent=5 -XX:MaxTenuringThreshold=5 -XX:+UnlockExperimentalVMOptions -XX:G1NewSizePercent=15 -XX:G1MaxNewSizePercent=50 -XX:InitiatingHeapOccupancyPercent=35"
#指定启动容器时执行的命令
CMD ["sh", "cloudserver.sh", "start"]