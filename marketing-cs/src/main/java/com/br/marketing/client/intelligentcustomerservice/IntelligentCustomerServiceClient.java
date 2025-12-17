package com.br.marketing.client.intelligentcustomerservice;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.cloud.counter.BrCounter;
import com.br.cloud.web.MethodType;
import com.br.cloud.web.PrometheusTimeMethod;
import com.br.common.log.AlertLog;
import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserDTO;
import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserTaskInfoDTO;
import com.br.marketing.client.intelligentcustomerservice.output.PolicyResultByTaskIdsDTO;
import com.br.marketing.client.net.ApiCaller;
import com.br.marketing.client.net.ApiCallerUtil;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.net.ThirdApiResultTransfer;
import com.br.marketing.entity.CustomerInfoPushLog;
import com.br.marketing.mapper.CustomerInfoPushLogMapper;
import com.br.marketing.mapper.datasource.log.InterfaceLogMapper;
import com.br.marketing.monitor.PrometheusMonitorUtils;
import com.marketingkit.tracking.model.indicator.DataFlowDirection;
import com.marketingkit.tracking.service.TrackingService;
import com.marketingkit.tracking.util.TrackingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

@Service
public class IntelligentCustomerServiceClient {

    private static final Logger logger = LoggerFactory.getLogger(IntelligentCustomerServiceClient.class);

    @Value("${api.intelligentCustomerService.pushUserUrl:00}")
    private String pushUrl;

    @Value("${api.customerService.apiCode:0}")
    private String customerServiceApiCode;

    @Autowired
    CustomerInfoPushLogMapper customerInfoPushLogMapper;

    @Autowired
    RestTemplate restTemplate;

    @Resource
    InterfaceLogMapper interfaceLogMapper;

    @Qualifier("interfaceLogDbpool")
    @Autowired
    ThreadPoolExecutor interfaceLogDbpool;

    @Resource
    TrackingService trackingService;

    @PrometheusTimeMethod(buckets = {0.02d, 0.05d, 0.2d, 0.5d, 1d}, methodType = MethodType.REMOTE)
    public Result<Integer> pushRuleCenterToPolicy(PushMarketingUserDTO dto, Long mId, String pushBatch, Integer pushNum) {
        dto.setPlatApiCode(customerServiceApiCode);
        Result result = new Result();
        CustomerInfoPushLog log = new CustomerInfoPushLog();
        log.setmId(mId);
        log.setBatch(pushBatch);
        String s = JSON.toJSONString(dto);
        log.setParam(s.length() > 4999 ? s.substring(0, 4999) : s);
//        log.setParam(s);
        log.setPushNum(pushNum);
//        log.setParam("");
        try {
            ThirdApiResultTransfer transfer = new ApiCaller(restTemplate).setUrl(pushUrl)
                    .setContentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .setEncode(Boolean.TRUE)
                    .setRequestParam(dto).postTransferStr();
            log.setResultContent(transfer.getResult().length() > 4999 ? transfer.getResult().substring(0, 4999) : transfer.getResult());
            log.setHttpStatus(String.valueOf(transfer.getHttpCode()));
            JSONObject jsonObject = JSON.parseObject(transfer.getResult());
            log.setCode(jsonObject.getString("code"));
            if (transfer.getHttpCode() != 200) {
                throw new RuntimeException(String.format("接口状态返回非200 是%d", transfer.getHttpCode()));
            }
            if ("00".equals(jsonObject.getString("code"))
                    || "900031".equals(jsonObject.getString("code"))) {
                result.setCode(ResultCode.SUCCESS.getValue());
                try {
                    //调用数量监控
                    BrCounter.count(PrometheusMonitorUtils.COUNT_POLICY_API_METRIC_NAME, dto.getApiCode(), "policy-api",
                            pushNum);
                    //region 埋点
                    try {
                        trackingService.trackDetailedLog(
                                DataFlowDirection.OUT
                                , dto.getApiCode()
                                , "推送决策"
                                , s
                                , Boolean.TRUE
                                , Long.valueOf(pushNum)
                                , TrackingContext.generateBatchId());
                    } catch (Exception ex) {
                        logger.warn(
                                AlertLog.buildWarnMessage(
                                        AlarmSendCodeEnum.TRACKING_POINT_SERVICEERROR.getCode()
                                        , ex.getMessage()
                                        , "埋点异常")
                                , ex);
                    }
                    //endregion
                } catch (Exception ex) {
                    logger.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_DECISIONERROR.getCode(), "推送决策接口统计异常!"), ex);
                }
            } else {
                result.setCode(ResultCode.FAIL.getValue()).setMessage(jsonObject.getString("message"));
            }
        } catch (ResourceAccessException e) {
//            // 这里捕获超时异常
//            if (e.getCause() instanceof ConnectTimeoutException) {
//                // 处理连接超时异常
//            } else if (e.getCause() instanceof ReadTimeoutException) {
//                // 处理读取超时异常
//            }
//            // 其他异常处理
            if (e.getMessage().contains("Read timed out")
//                    || e.getMessage().contains("Connect timed out")
            ) {
                // 处理超时异常...
                result.setCode(ResultCode.TIME_OUT.getValue()).setMessage("Read timed out");
                log.setErrorContent(e.getMessage());
            } else {
                // 处理其他类型的ResourceAccessException...
                log.setErrorContent(e.getMessage());
                result.setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue()).setMessage(e.getMessage());
            }
        } catch (Exception ex) {
            log.setErrorContent(ex.getMessage());
            result.setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue()).setMessage(ex.getMessage());
        }
        log.setCreateTime(new Date());
        customerInfoPushLogMapper.insertSelective(log);
        return result;
    }

    @PrometheusTimeMethod(buckets = {0.02d, 0.05d, 0.2d, 0.5d, 1d}, methodType = MethodType.REMOTE)
    public Result pushUser(PushMarketingUserDTO dto) {
        Result result = new Result();
        dto.setPlatApiCode(customerServiceApiCode);
        try {
            ThirdApiResultTransfer transfer = new ApiCallerUtil(restTemplate, interfaceLogMapper, interfaceLogDbpool)
                    .setUrl(pushUrl).setContentType(MediaType.APPLICATION_FORM_URLENCODED).setRequestParam(dto).setEncode(Boolean.TRUE).postTransferStr();
            JSONObject jsonObject = JSON.parseObject(transfer.getResult());
            if (transfer.getHttpCode() != 200) {
                result.setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
                return result;
            }
            if ("00".equals(jsonObject.getString("code"))
                    || "900031".equals(jsonObject.getString("code"))) {
                result.setCode(ResultCode.SUCCESS.getValue());
                try {
                    //监控
                    PushMarketingUserTaskInfoDTO taskInfoDTO = (PushMarketingUserTaskInfoDTO) dto.getJsonData();
                    BrCounter.count(PrometheusMonitorUtils.COUNT_POLICY_API_METRIC_NAME, dto.getApiCode(), "policy-api",
                            taskInfoDTO.getData().size());
                    //region 埋点
                    try {
                        trackingService.trackDetailedLog(
                                DataFlowDirection.OUT
                                , dto.getApiCode()
                                , "推送决策"
                                , JSON.toJSONString(dto.getJsonData())
                                , Boolean.TRUE
                                , Long.valueOf(taskInfoDTO.getData().size())
                                , TrackingContext.generateBatchId());
                    }catch (Exception ex){
                        logger.warn(
                                AlertLog.buildWarnMessage(
                                        AlarmSendCodeEnum.TRACKING_POINT_SERVICEERROR.getCode()
                                        , ex.getMessage()
                                        , "埋点异常")
                                , ex);
                    }
                    //endregion
                } catch (Exception ex) {
                    logger.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_DECISIONERROR.getCode(), "推送决策接口异常!"), ex);
                }
            } else {
                result.setCode(ResultCode.FAIL.getValue()).setMessage(jsonObject.getString("message"));
            }
        } catch (Exception ex) {
            logger.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_DECISIONERROR.getCode(), ex.getMessage()), ex);
            result.setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue()).setMessage(ex.getMessage());
        }
        return result;
    }

    @PrometheusTimeMethod(buckets = {0.02d, 0.05d, 0.2d, 0.5d, 1d}, methodType = MethodType.REMOTE)
    public Result getReachStrategy(PushMarketingUserDTO dto) {
        dto.setPlatApiCode(customerServiceApiCode);
        Result result = new Result();
        try {
            ThirdApiResultTransfer transfer = new ApiCaller(restTemplate).setUrl(pushUrl)
                    .setContentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .setEncode(Boolean.TRUE)
                    .setRequestParam(dto).postTransferStr();
            JSONObject jsonObject = JSON.parseObject(transfer.getResult());
            if (transfer.getHttpCode() != 200) {
                throw new RuntimeException(String.format("接口状态返回非200 是%d", transfer.getHttpCode()));
            }
            if ("00".equals(jsonObject.getString("code"))
                    || "900031".equals(jsonObject.getString("code"))) {
                result.setDate(jsonObject);
                result.setCode(ResultCode.SUCCESS.getValue());
            } else {
                result.setCode(ResultCode.FAIL.getValue()).setMessage(jsonObject.getString("message"));
            }
        } catch (Exception e) {

        }
        return result;
    }

    public Result<String> getUserStatus(PushMarketingUserDTO dto) {
        dto.setPlatApiCode(customerServiceApiCode);
        Result<String> result = new Result();
        try {
            ThirdApiResultTransfer transfer = new ApiCaller().setUrl(pushUrl)
                    .setContentType(MediaType.MULTIPART_FORM_DATA)
                    .setRequestParam(dto).postTransferStr();
            if (transfer.getHttpCode() != 200) {
                result.setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue()).setMessage(transfer.getResult());
            } else {
                JSONObject jsonObject = JSON.parseObject(transfer.getResult());
                result.setCode(ResultCode.SUCCESS.getValue()).setDate(jsonObject.getString("code"));
                result.setMessage(jsonObject.getString("data"));
            }
        } catch (Exception ex) {
            result.setCode(ResultCode.FAIL.getValue()).setMessage(ex.getMessage());
        }
        return result;
    }

    public Result<List<PolicyResultByTaskIdsDTO>> getTaskIdsResult(String apiCode, List<String> taskIds) {
        PushMarketingUserDTO dto = new PushMarketingUserDTO();
        JSONObject jsonData = new JSONObject();
        jsonData.put("method", "uploadVerification");
        JSONObject taskIdJsons = new JSONObject();
        taskIdJsons.put("taskIds", taskIds);
        jsonData.put("data", taskIdJsons);
        dto.setJsonData(jsonData);
        dto.setApiCode(apiCode);
        dto.setPlatApiCode(customerServiceApiCode);
        Result<List<PolicyResultByTaskIdsDTO>> result = new Result();
        try {
            ThirdApiResultTransfer transfer = new ApiCaller(restTemplate).setUrl(pushUrl)
                    .setContentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .setEncode(Boolean.TRUE)
                    .setRequestParam(dto).postTransferStr();
            JSONObject jsonObject = JSON.parseObject(transfer.getResult());
            if (transfer.getHttpCode() != 200) {
                result.setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue()).setMessage(transfer.getResult());
                return result;
            }
            if ("00".equals(jsonObject.getString("code"))) {
                result.setCode(ResultCode.SUCCESS.getValue()).setDate(JSONObject.parseArray(jsonObject.getJSONArray("data").toJSONString(),
                        PolicyResultByTaskIdsDTO.class));
            } else {
                result.setCode(ResultCode.FAIL.getValue()).setMessage(jsonObject.getString("message"));
            }

        } catch (Exception ex) {
            result.setCode(ResultCode.FAIL.getValue()).setMessage(ex.getMessage());
        }
        return result;
    }


}
