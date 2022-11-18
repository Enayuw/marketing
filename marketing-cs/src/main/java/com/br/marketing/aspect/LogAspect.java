package com.br.marketing.aspect;

import com.alibaba.fastjson.JSON;
import com.br.marketing.common.commondto.ApiNoDataResult;
import com.br.marketing.common.utils.IpUtil;
import com.br.marketing.context.RuntimeDataContext;
import com.br.marketing.dto.ResponseCustomDTO;
import com.br.marketing.dto.shuhe.Response2ShuheDTO;
import com.br.marketing.dto.shuhe.ResponseShuheDTO;
import com.br.marketing.entity.MarketingInfoLog;
import com.br.marketing.rpcclient.RpcClientProxy;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
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
     * 后置调用 数禾订制
     *
     * @param
     * @return
     */
    @AfterReturning(returning = "res", pointcut = "execution (public * com.br.marketing.*.controller.MarketingTransferDataController.receiveShuheTransferDataSync(..))" +
            "|| execution(public * com.br.marketing.*.controller.MarketingUserPreController.receiveShuHeUploadData(..))")
    public void doAfterReturning(ResponseCustomDTO res) {
        try {
            MarketingInfoLog uploadLog = RuntimeDataContext.getData();
            uploadLog.setResponseJson(JSON.toJSONString(res));
            long endTime = System.currentTimeMillis();
            uploadLog.setEndTime(endTime);
            long costTime = endTime - RuntimeDataContext.getData().getStartTime();
            uploadLog.setCostTime(costTime);
            if (res instanceof Response2ShuheDTO) {
                Response2ShuheDTO response2ShuheDTO = (Response2ShuheDTO) res;
                uploadLog.setResponseCode(String.valueOf(response2ShuheDTO.getCode()));
                if (costTime > 1000) {
                    log.warn("request_batch:{},response code:{},message:{},msgId:{},cost_time:{}",
                            RuntimeDataContext.getData().getRequestBatch(), uploadLog.getResponseCode()
                            , response2ShuheDTO.getDesc(), response2ShuheDTO.getMsgId(), costTime);
                }
            } else if (res instanceof ResponseShuheDTO) {
                ResponseShuheDTO responseShuheDTO = (ResponseShuheDTO) res;
                uploadLog.setResponseCode(String.valueOf(responseShuheDTO.getCode()));
                if (costTime > 1000) {
                    log.warn("request_batch:{},response code:{},message:{},cost_time:{}",
                            RuntimeDataContext.getData().getRequestBatch(), uploadLog.getResponseCode()
                            , responseShuheDTO.getDesc(), costTime);
                }
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
        RpcClientProxy.sendUploadLog(content);
    }
}
