package com.br.marketing.client;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.cloud.counter.BrCounter;
import com.br.cloud.web.MethodType;
import com.br.cloud.web.PrometheusTimeMethod;
import com.br.common.log.AlertLog;
import com.br.marketing.client.net.ApiCallerUtil;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.net.ThirdApiResultTransfer;
import com.br.marketing.mapper.datasource.log.InterfaceLogMapper;
import com.br.marketing.monitor.PrometheusMonitorUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.concurrent.ThreadPoolExecutor;

/**发送邮件客户端
 * @author 10400
 * @create 2017-06-27 13:33
 */
@Service
@Slf4j
public class HaloCallBackDataApiClient {
    @Value("${api.halo.openUrl:00}")
    private String haloCallBackUrl;

    @Autowired
    RestTemplate restTemplate;

    @Resource
    InterfaceLogMapper interfaceLogMapper;

    @Qualifier("interfaceLogDbpool")
    @Autowired
    ThreadPoolExecutor interfaceLogDbpool;


    @PrometheusTimeMethod(buckets = {0.02d, 0.05d, 0.2d, 0.5d, 1d}, methodType = MethodType.REMOTE)
    public Result dealMarketingCallBack(String apiCode,JSONObject requestJson) {
       Result result = new Result();
        try {
            ThirdApiResultTransfer transfer = new ApiCallerUtil(restTemplate,interfaceLogMapper,interfaceLogDbpool).setUrl(haloCallBackUrl)
                    .setContentType(MediaType.APPLICATION_JSON).setEncode(true)
                    .setRequestParam(requestJson).postTransferStr();
            JSONObject jsonObject = JSON.parseObject(transfer.getResult());
            log.warn("dealMarketingCallBack:{}",jsonObject.toJSONString());
            if (transfer.getHttpCode() != 200) {
                result.setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
                result.setMessage(JSONObject.toJSONString(transfer));
                return result;
            }
            if ("10000".equals(jsonObject.getString("code"))) {
                result.setCode(ResultCode.SUCCESS.getValue());
                try {
                    //监控
                    BrCounter.count(PrometheusMonitorUtils.COUNT_HALO_CALLBACK_API_METRIC_NAME, apiCode, "halo-callback-api",
                            requestJson.size());
                } catch (Exception ex) {
                    log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_DECISIONERROR.getCode(), "哈啰-三方营销数据回传异常!"), ex);
                }
            } else {
                result.setCode(ResultCode.FAIL.getValue()).setMessage(transfer.getResult());
            }
        } catch (Exception ex) {
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.HALUO_CALLBACK_DATA_INTERFACEERROR.getCode(), ex.getMessage()), ex);
            result.setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue()).setMessage(ex.getMessage());
        }
        return result;
    }

}
