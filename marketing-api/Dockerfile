#指定基础镜像
FROM registry.100credit.cn/centos6/jdk_8u191:v1.0
#Dockerfile使用变量（可以在Dockerfile中进行引用）
ARG APPNAME=loan-warning-api
ARG APP_HOME=/opt/SpringCloud
#工作目录
WORKDIR $APP_HOME
#指定appname标签
LABEL appname=$APPNAME \
#指定version标签
version=1.0.0
#把在宿主机上编译好的jar包复制到容器内的指定地址，格式为”COPY [宿主机目录] [容器内目录]“，注意宿主机目录一定是Dockerfile所在目录的相对目录
COPY loan-warning-api/target/loan-warning-api*.jar $APP_HOME/app/$APPNAME/lib/
#拷贝不同环境配置文件到config目录
COPY loan-warning-api/src/main/resources/ $APP_HOME/app/$APPNAME/config/
#拷贝启动文件到 工作目录
COPY loan-warning-api/cloudserver.sh $APP_HOME
#环境变量配置
#0为注册中心，1为非注册中心应用
ENV APP_TYPE=1 \
#HOME目录
APP_HOME=$APP_HOME \
#服务目录
SERVICE_HOME=$APP_HOME/app/$APPNAME \
#启动类
MAIN_CLASS=com.br.loan.warning.api.StrategyEarlyWaringApiApplication \
#application文件名
APP_CONFIG=application.yml \
#是否依赖公共jar, 1 依赖 2 不依赖
DEPEND_COMMON=2 \
#启动参数
#APP_PARAM="-Xmx2g -Xms2g -XX:NewRatio=4  -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=128m -Xss256k -XX:+UseConcMarkSweepGC -XX:+CMSParallelRemarkEnabled"
APP_PARAM="-Xmx4g -Xms4g -XX:MetaspaceSize=256m -XX:MaxMetaspaceSize=256m -Xss256k -XX:+UseG1GC -XX:ConcGCThreads=2 -XX:ParallelGCThreads=4 -XX:MaxGCPauseMillis=100"
#指定启动容器时执行的命令
CMD ["sh", "cloudserver.sh", "start"]