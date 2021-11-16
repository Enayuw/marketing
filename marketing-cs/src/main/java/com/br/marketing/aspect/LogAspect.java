package com.br.marketing.aspect;

import Ice.AsyncResult;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.bsf.ext.app.util.Ice1BSFConsumerBean;
import com.br.marketing.common.commondto.ApiNoDataResult;
import com.br.marketing.common.utils.IpUtil;
import com.br.marketing.context.RuntimeDataContext;
import com.br.marketing.entity.MarketingInfoLog;
import com.br.mom.v3.broker_layer_api.api.BrokerLayerServicePrx;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 切面
 *
 * @Author linquan.guo
 * @CreateDate 2021/11/3 15:06
 * @UpdateUser linquan.guo
 * @UpdateDate 2021/11/3 15:06
 * @UpdateRemark 修改内容
 * @Version 1.0
 */
@Component
@Aspect
@Slf4j
@Order(-1)
public class LogAspect {
    @Value("${otherConfig.uploadMom.producerKey:00}")
    private String producerKey;
    @Value("${otherConfig.uploadMom.appSecretKey:00}")
    private String appSecretKey;
    @Value("${otherConfig.uploadMom.destinationName:00}")
    private String destinationName;
    @Value("${otherConfig.uploadMom.logIceTimeout:00}")
    private int logIceTimeout;

    /**
     * 方法
     *
     * @param
     * @return
     */
    @Pointcut("@annotation(com.br.marketing.aspect.LogAnnotation)")
    public void pointCut() {

    }

    /**
     * 前置调用
     *
     * @param
     * @return
     */
    @Before("pointCut()")
    public void before() {
        RuntimeDataContext.initData();
        //请求时间
        long startTime = System.currentTimeMillis();
        RuntimeDataContext.getData().setStartTime(startTime);
        //获取ip
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String ip = IpUtil.getRemortIP(attributes.getRequest());
        RuntimeDataContext.getData().setRequestIp(ip);
    }

    /**
     * 后置调用
     *
     * @param
     * @return
     */
    @AfterReturning(
            returning = "ret",
            pointcut = "pointCut()"
    )
    public void doAfterReturning(ApiNoDataResult ret) {
        try {
            MarketingInfoLog uploadLog = RuntimeDataContext.getData();
            uploadLog.setResponseJson(JSON.toJSONString(ret));
            uploadLog.setResponseCode(ret.getCode());
            long endTime = System.currentTimeMillis();
            uploadLog.setEndTime(endTime);
            long costTime = endTime - RuntimeDataContext.getData().getStartTime();
            uploadLog.setCostTime(costTime);
            if (costTime > 1000) {
                log.warn("request_batch:{},response code:{},message:{},cost_time:{}",
                        RuntimeDataContext.getData().getRequestBatch(), ret.getCode(), ret.getMessage(), costTime);
            }
            sendUploadLog(JSON.toJSONString(uploadLog));
        } catch (Exception e) {
            log.error("pointCut error", e);
        } finally {
            RuntimeDataContext.removeData();
        }
    }

    /**
     * 上传日志发送mom
     *
     * @param content
     * @return
     */
    public void sendUploadLog(String content) {
        String param = null;
        try {
            BrokerLayerServicePrx service = (BrokerLayerServicePrx) Ice1BSFConsumerBean
                    .getServiceProxy(BrokerLayerServicePrx.class, "V3.0.0");
            //超时时间
            service.ice_invocationTimeout(logIceTimeout);
            //请求参数
            JSONObject paramJson = new JSONObject();
            paramJson.put("appName", producerKey);
            paramJson.put("appSecretKey", appSecretKey);
            JSONObject requestData = new JSONObject();
            requestData.put("destinationName", destinationName);
            //入参内容
            requestData.put("content", content);
            paramJson.put("requestData", requestData);
            param = paramJson.toJSONString();
            AsyncResult beginSender = service.begin_sender(param);
            log.warn("userReportLog mom request return : {}", beginSender == null ? "" : beginSender.isSent());
        } catch (Exception e) {
            log.error("userReportLog mom request Error：{}" + param, e);
        }
    }
}
