#!/bin/bash
echo $*

echo 'options single-request-reopen' >> /etc/resolv.conf

APP_TYPE=${APP_TYPE}
[ ! -z "$APP_TYPE" ] || { echo "APP_TYPE 不能为空，请配置APP_TYPE，0为注册中心，1为非注册中心应用。";
        if [ "$1" = "stop" ]; then exit 0;
        else exit 1; fi; }

CLOUDSERVER_HOME=${APP_HOME}
[ -d "$CLOUDSERVER_HOME" ] || { echo "CLOUDSERVER_HOME 路径不存在: $CLOUDSERVER_HOME";
        if [ "$1" = "stop" ]; then exit 0;
        else exit 1; fi; }

CONF_ENV=${CONF_ENV}
[ ! -z "$CONF_ENV" ] || { echo "环境变量 CONF_ENV 为空或者配置错误！";
                if [ "$1" = "stop" ]; then exit 0;
        else exit 1; fi; }

LOG_LEVEL=${LOG_LEVEL}
[ ! -z "$LOG_LEVEL" ] || { echo "环境变量 LOG_LEVEL 为空或者配置错误！";
               if [ "$1" = "stop" ]; then exit 0;
        else exit 1; fi; }

POD_NAME=${POD_NAME}
[ ! -z "$POD_NAME" ] || { echo "环境变量 POD_NAME 为空或者配置错误！";
               if [ "$1" = "stop" ]; then exit 0;
        else exit 1; fi; }

APP_CONFIG=${APP_CONFIG}
[ ! -z "$APP_CONFIG" ] || { echo " APP_CONFIG 为空或者配置错误！${APP_CONFIG}";
               if [ "$1" = "stop" ]; then exit 0;
        else exit 1; fi; }

SERVICE_HOME=${SERVICE_HOME}
[ ! -z "$SERVICE_HOME" ] || { echo "SERVICE_HOME 该文件不存在或者没有权限: $SERVICE_HOME";
        if [ "$1" = "stop" ]; then exit 0;
        else exit 5; fi; }

NAME=${APPNAME}
export NAME
[ ! -z "$NAME" ] || { echo "环境变量 APPNAME 为空或者配置错误！";
        if [ "$1" = "stop" ]; then exit 0;
       else exit 5; fi; }

PORT=${SERVER_PORT}

[ ! -z "$PORT" ] || { echo "环境变量SPRING_APPLICATION_PORT为空或者配置错误！";
       if [ "$1" = "stop" ]; then exit 0;
       else exit 5; fi; }

NAME_UPPER="$(echo $NAME| tr '[:lower:]' '[:upper:]')" 

HOSTNAME=`hostname`

[ ! -z "$HOSTNAME" ] || { echo "hostname 配置错误，请检查/etc/hosts 文件配置。";
        if [ "$1" = "stop" ]; then exit 0;
        else exit 5; fi; }

CLOUDSERVER_PID_FILE="$CLOUDSERVER_HOME/pid"

CLOUDSERVER_JAVA_CMD="$JAVA_HOME/bin/java"

GC_LOG_PATH="$CLOUDSERVER_HOME/logs/$NAME/$POD_NAME"
{ ls $GC_LOG_PATH &>/dev/null || { echo "pod子文件夹不存在，开始创建... ...";mkdir -p $GC_LOG_PATH && echo "创建pod子文件夹成功！" || exit 1; };  }

# ================ JVM参数配置功能（最高优先级）================
# JVM配置文件路径（固定文件名）
JVM_CONFIG_FILE="$SERVICE_HOME/jvm-config/jvm-options.sh"

# 保存默认的APP_PARAM（在加载配置文件前保存）
DEFAULT_APP_PARAM="${APP_PARAM:-}"

echo "=========================================="
echo "JVM配置检查开始..."
echo "SERVICE_HOME: $SERVICE_HOME"
echo "JVM配置文件路径: $JVM_CONFIG_FILE"

# 检查JVM配置文件是否存在
if [ -f "$JVM_CONFIG_FILE" ]; then
    echo ">> 检测到JVM配置文件，使用最高优先级配置"
    echo ">> 配置文件: $JVM_CONFIG_FILE"

    # 检查文件是否可读和可执行
    if [ -r "$JVM_CONFIG_FILE" ] && [ -x "$JVM_CONFIG_FILE" ]; then
        echo ">> 配置文件权限检查通过"

        # 加载JVM配置文件，完全覆盖原有配置
        echo ">> 正在加载JVM配置文件..."
        source "$JVM_CONFIG_FILE"

        # 检查APP_PARAM是否被正确设置
        if [ -n "$APP_PARAM" ]; then
            echo ">> JVM配置文件加载成功"
            echo ">> 最终使用的APP_PARAM长度: ${#APP_PARAM} 字符"
            # 只显示前100个字符避免输出过长
            echo ">> APP_PARAM preview: ${APP_PARAM:0:100}..."
        else
            echo ">> 警告: JVM配置文件中APP_PARAM为空，使用默认配置"
            APP_PARAM="$DEFAULT_APP_PARAM"
        fi
    else
        echo ">> 错误: JVM配置文件权限不足，请检查文件权限"
        echo ">> 使用默认APP_PARAM配置"
        APP_PARAM="$DEFAULT_APP_PARAM"
    fi
else
    echo ">> JVM配置文件不存在: $JVM_CONFIG_FILE"
    echo ">> 使用默认APP_PARAM配置"
    APP_PARAM="$DEFAULT_APP_PARAM"
fi

# 设置JAVA_OPTIONS
JAVA_OPTIONS="${APP_PARAM} -Xloggc:$GC_LOG_PATH/gc.log "

echo ">> 最终JAVA_OPTIONS长度: ${#JAVA_OPTIONS} 字符"
echo ">> JAVA_OPTIONS preview: ${JAVA_OPTIONS:0:150}..."
echo "JVM配置检查完成"
echo "=========================================="
# ================ JVM参数配置功能结束 ================

APP_JAR_NAME=`ls $SERVICE_HOME/lib/*.jar | awk -F'[/]+' {'print $NF'}`
[ ! -z "$APP_JAR_NAME" ] || { echo "APP_JAR_NAME为空或者配置错误！";
                if [ "$1" = "stop" ]; then exit 0;
        else exit 5; fi; }

PINPOINT_ENABLE=${PINPOINT_ENABLE}
PINPOINT_OPTIONS_VER=${PINPOINT_OPTIONS_VER}
PINPOINT_OPTIONS=""
if [[ $PINPOINT_ENABLE == 'true' ]] ; then
    SPEED_ENV=${SPEED_ENV}
    [ ! -z "$SPEED_ENV" ] || { echo "PINPOINT已经开启，但是SPEED_ENV没有配置";
        if [ "$1" = "stop" ]; then exit 0;
        else exit 5; fi; }
    echo "开启Pinpoint"
    AGENT_ID=`echo -n $POD_NAME|openssl dgst -md5 -binary|base64|sed "s/+/-/g;s/\//_/g;s/=//g"`
    if [[ "${PINPOINT_OPTIONS_VER}" == "new" ]] ; then
        PINPOINT_OPTIONS=" -javaagent:/opt/springcloud/data/pinpoint-1.8.4/pinpoint-bootstrap-1.8.4.jar -Dpinpoint.agentId=$AGENT_ID -Dpinpoint.applicationName=$NAME-$SPEED_ENV "
    else
        PINPOINT_OPTIONS=" -javaagent:/opt/springcloud/data/pinpoint/pinpoint-bootstrap-1.8.4.jar -Dpinpoint.agentId=$AGENT_ID -Dpinpoint.applicationName=$NAME-$SPEED_ENV "
    fi
else
    echo "未开启Pinpoint"
fi

if [[ $RPC_MODE == 'ISTIO_ETCD' ]] ; then
    TTL_AGENT="-javaagent:/opt/springcloud/data/ttl-agent/transmittable-thread-local-2.11.5.jar"
fi

#Rasp
RASP_OPTIONS=""
if [[ $RASP_ENABLE == 'true' ]] ; then
    tar -zxf /opt/springcloud/data/OpenRasp/rasp-java.tar.gz -C /tmp \
    && mv /tmp/rasp-*/rasp /rasp \
    && echo "cloud.enable: true" >> /rasp/conf/openrasp.yml \
    && echo "cloud.backend_url: ${RASP_BACKEND_URL}" >> /rasp/conf/openrasp.yml \
    && echo "cloud.app_id: ${RASP_APP_ID}" >> /rasp/conf/openrasp.yml \
    && echo "cloud.app_secret: ${RASP_APP_SECRET}" >> /rasp/conf/openrasp.yml \
    && RASP_OPTIONS="-javaagent:/rasp/rasp.jar"
else
    echo "未开启Rasp"
fi

MAIN_CLASS=${MAIN_CLASS}

DEPEND_COMMON=${DEPEND_COMMON}

SPEED_ENV=${SPEED_ENV}
[ ! -z "$SPEED_ENV" ] || { echo "环境变量 SPEED_ENV 为空或者配置错误！";
        if [ "$1" = "stop" ]; then exit 0;
        else exit 1; fi; }

echo $MAIN_CLASS

JAVA_CMD="$CLOUDSERVER_JAVA_CMD $TTL_AGENT $JAVA_OPTIONS $PINPOINT_OPTIONS $RASP_OPTIONS -Dspeed.env=$SPEED_ENV -DjarPath=$SERVICE_HOME/lib/$APP_JAR_NAME -Xbootclasspath/a:$SERVICE_HOME/config/$CONF_ENV  -jar $SERVICE_HOME/lib/$APP_JAR_NAME "
#JAVA_CMD="$CLOUDSERVER_JAVA_CMD $JAVA_OPTIONS $PINPOINT_OPTIONS $RASP_OPTIONS -Dspeed.env=$SPEED_ENV -DjarPath=$SERVICE_HOME/lib/$APP_JAR_NAME -jar $SERVICE_HOME/lib/$APP_JAR_NAME "

PARAMS=" --server.tomcat.max-threads=1000"

RETVAL=0

#检查端口是否被监听方法
function checkport(){
    PID=$1
    num=`netstat -ntpl | grep $PID/ | wc -l`
    print_comm="netstat -ntpl 2>&1 | grep $PID/ "
    echo "检查端口监听状态，请稍等!"
    while [[ $num -le 0 ]];
    do
        echo -ne "."
        sleep 1
        num=`netstat -ntpl | grep $PID/ | wc -l`
        if [ $num -gt 0 ];then
            echo ""
            echo "端口监听信息如下："
            printline= eval "$print_comm"
            echo $printline
        fi
    done

}

#查询注册中心状态
function getstatus() {
  APPSTATUS=`curl -XGET ${URL}apps -s | egrep '<instanceId>|<status>' |sed '1,$s/\s\+<instanceId>\|\s\+<status>\|<\/status>//g' |sed 'N;s/<\/instanceId>\n/ /g' | grep ${APPNAME} |awk {'print $2'}`
  echo ${APPSTATUS}
}


#启动服务方法
function start() {
    START_COMM="$JAVA_CMD $PARAMS &"
    echo "执行启动命令:[$START_COMM]"
    eval "$START_COMM"
    RETVAL=$?
    if [ $RETVAL = 0 ]; then
        PID=$!
        echo $PID > "$CLOUDSERVER_PID_FILE"
        echo "执行启动命令成功！"
        wait $PID
    else
        echo "failure"
    fi
}

#停止服务方法
function stop() {
    if [ $APP_TYPE != 0 ]; then
        sleep 5
        cd /opt/SpringCloud/logs/${NAME}/  &&  mv ${POD_NAME} ${POD_NAME}_$(date +%Y%m%d)
    fi
    kill -9 `cat "$CLOUDSERVER_PID_FILE"`
}

case $1 in
    start)
        start
        ;;
    stop)
        stop
        ;;
    restart)
        stop
        start
        ;;
    *)
        echo "Usage: $0 {start|stop|status|try-restart|restart|force-reload|reload|probe}"
        exit 1
        ;;

esac

exit $RETVAL
