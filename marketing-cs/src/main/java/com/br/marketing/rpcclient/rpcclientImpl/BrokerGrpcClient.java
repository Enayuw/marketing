package com.br.marketing.rpcclient.rpcclientImpl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.grpc.mom.broker_layer_api.BrokerLayerGrpc;
import com.br.grpc.mom.broker_layer_api.ResponseBean;
import com.br.grpc.mom.broker_layer_api.SendRequest;
import com.br.marketing.common.utils.net.InterfaceLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.RequestLog;
import com.br.marketing.rpcclient.GrpcClientInitConfig;
import com.br.transfer.guava.BrCallbackListener;
import com.google.common.util.concurrent.FutureCallback;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.ThreadPoolExecutor;

@Component
@Slf4j
public class BrokerGrpcClient {

    //必须添加且不能变动,logback.xml会根据名称重新转入.mom后缀文件中输出并进行收集ELK收集
    private static Logger momLog = LoggerFactory.getLogger("MOM_LOG");
    //自定义配置callback异常处理线程池，线程池和线程池参数仅供参考，请根据自己的业务使用，各业务人员可根据自己业务程度选择队列满了之后的操作行为：
    private static ThreadPoolExecutor callBackExecutor = BrExecutors.getThreadPool(
            2, 4, "mom-call-back-executor", 1000);


    private static String upload_producerKey;

    private static String upload_appSecretKey;

    private static String upload_destinationName;

    private static int upload_logIceTimeout;

    /*推送日志app名称*/
    private static String push_appName;

    /*推送日志mom加密key*/
    private static String push_appSecretKey;

    /*推送日志db表名*/
    private static String push_destinationName;

    @Value("${otherConfig.uploadMom.producerKey:00}")
    public void setUpload_producerKey(String upload_producerKey) {
        BrokerGrpcClient.upload_producerKey = upload_producerKey;
    }

    @Value("${otherConfig.uploadMom.appSecretKey:00}")
    public void setUpload_appSecretKey(String upload_appSecretKey) {
        BrokerGrpcClient.upload_appSecretKey = upload_appSecretKey;
    }

    @Value("${otherConfig.uploadMom.destinationName:00}")
    public void setUpload_destinationName(String upload_destinationName) {
        BrokerGrpcClient.upload_destinationName = upload_destinationName;
    }

    @Value("${otherConfig.uploadMom.logIceTimeout:00}")
    public void setUpload_logIceTimeout(int upload_logIceTimeout) {
        BrokerGrpcClient.upload_logIceTimeout = upload_logIceTimeout;
    }

    @Value("${otherConfig.mom.appName:00}")
    public void setPush_appName(String push_appName) {
        BrokerGrpcClient.push_appName = push_appName;
    }

    @Value("${otherConfig.mom.appSecretKey:00}")
    public void setPush_appSecretKey(String push_appSecretKey) {
        BrokerGrpcClient.push_appSecretKey = push_appSecretKey;
    }

    @Value("${otherConfig.mom.destinationName:00}")
    public void setPush_destinationName(String push_destinationName) {
        BrokerGrpcClient.push_destinationName = push_destinationName;
    }

    public static void sendUploadLog(String content) {
        String param = null;
        try {
            //请求参数
            JSONObject paramJson = new JSONObject();
            paramJson.put("appName", upload_producerKey);
            paramJson.put("appSecretKey", upload_appSecretKey);
            JSONObject requestData = new JSONObject();
            requestData.put("destinationName", upload_destinationName);
            //入参内容
            requestData.put("content", content);
            paramJson.put("requestData", requestData);
            paramJson.put("swiftNum", UUID.randomUUID().toString().replaceAll("-",""));
            param = paramJson.toJSONString();
            sendFuture(param);
            log.warn("userReportLog mom request return：future");
        } catch (Exception e) {
            log.error("userReportLog mom request Error：{}" + param, e);
        }
    }

    /**
     * 推送调用下游接口日志
     *
     * @param requestLog
     */
    public static void sendRequestLog(RequestLog requestLog) {
        JSONObject paramJson = new JSONObject();
        JSONObject requestData = new JSONObject();
        requestData.put("destinationName", push_destinationName);
        paramJson.put("appName", push_appName);
        paramJson.put("appSecretKey", push_appSecretKey);
        String param = JSON.toJSONString(requestLog);
        paramJson.put("swiftNum", UUID.randomUUID());
        requestData.put("content", param);
        paramJson.put("requestData", requestData);
        //log.warn("MQ入参--{}",paramJson);
        try {
            sendFuture(paramJson.toString());
            log.info("pushLog mom request return : future");
        } catch (Exception e) {
            log.error("日志信息写入消息队列异常", e);
        }
    }

    /**
     * 推送调用下游接口日志
     * @param interfaceLog
     */
    public static void sendInterfaceLog(InterfaceLog interfaceLog) {
        JSONObject paramJson = new JSONObject();
        JSONObject requestData = new JSONObject();
        requestData.put("destinationName", push_destinationName);
        paramJson.put("appName", push_appName);
        paramJson.put("appSecretKey", push_appSecretKey);
        String param = JSON.toJSONString(interfaceLog);
        paramJson.put("swiftNum", UUID.randomUUID());
        requestData.put("content", param);
        paramJson.put("requestData", requestData);
        //log.warn("MQ入参--{}",paramJson);
        try {
            sendFuture(paramJson.toString());
            log.info("pushLog mom request return : future");
        } catch (Exception e) {
            log.error("日志信息写入消息队列异常", e);
        }
    }

    private static void sendFuture(final String msg) {
        BrokerLayerGrpc.BrokerLayerFutureStub brokerLayerFutureStub = GrpcClientInitConfig.grpcBroker();
        SendRequest sendrequest = SendRequest.newBuilder().setMsg(msg).build();
        ListenableFuture<ResponseBean> listenableFuture = brokerLayerFutureStub.sendMsg(sendrequest);
        listenableFuture.addListener(BrCallbackListener.getTtlCallbackListener(listenableFuture, new FutureCallback<ResponseBean>() {
            @Override
            public void onSuccess(ResponseBean result) {
                //此处提供测试验证，开发人员、测试人员通过这块看ELK/消费端对应索引是否有日志判断验证整个流程是否正常
                //可通过yaml中增加环境变量MOM_TEST=true来触发此处打印日志仅用来验证用，注意header包引入后如果DLF=off则此处的info日志才可触发，如果预发验证无问题，上线此处可去掉
//                momLog.warn("[test verify mom log result]{}message:{}code:{}", result.getResult(),result.getMessage(),result.getCode());
//                log.warn("[test verify mom log result]{}message:{}code:{}", result.getResult(),result.getMessage(),result.getCode());
                if (!"0".equals(result.getCode())) {
                    //建议返回码非0的时候配置响应的阶梯告警
                    String msgLog = AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_COMMON.getCode()
                            , "返回的异常码值："+result.getCode()+"发送的消息："+msg
                            , "MOM日志发送完成，但是码值异常");
                    log.warn(msgLog);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                // 业务可以自定义消息异常以及报警,禁止使用momLog进行日志打印 建议返回码非0的时候配置响应的阶梯告警
                String msgLog = AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_COMMON.getCode()
                        , "发送的消息：["+msg+"]异常信息："+throwableToString(t)
                        , "MOM日志发送异常");
                log.warn(msgLog);
                //异常日志打印，后期进行补偿,日志会单独输出到.mom中，此处各业务人员不能修改，此处打印日志
                momLog.error("[compensate data]{}", msg);
            }
        }), callBackExecutor);
    }

    public static String throwableToString(Throwable e) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(1048576);
        PrintStream printStream = new PrintStream(outputStream);
        e.printStackTrace(printStream);
        return new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
    }
}
