package com.br.marketing.client.didi;

import com.alibaba.fastjson.JSON;
import com.br.cloud.web.MethodType;
import com.br.cloud.web.PrometheusTimeMethod;
import com.br.common.log.AlertLog;
import com.br.marketing.aspect.Mockable;
import com.br.marketing.client.HttpProxyClient;
import com.br.marketing.client.didi.input.v5.DiDiV5CollidingRequestDTO;
import com.br.marketing.client.didi.output.v5.DiDiV5CollidingResultResponseDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.constants.MockConstants;
import java.util.HashMap;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DiDiV5Client {

    @Value("${api.didi.collidingUrl:https://admarketing-manhattan.xiaojukeji.com/crow/collision/mediaName}")
    private String collidingUrl;
    @Value("${api.didi.isProxy:false}")
    private Boolean isProxy;
    @Resource
    private HttpProxyClient httpProxyClient;


    @PrometheusTimeMethod(buckets = {0.02d, 0.05d, 0.2d, 0.5d, 1d}, methodType = MethodType.REMOTE)
    @Mockable(mockName = MockConstants.TEST_DIDI_V5_COLLIDING_DATA_RETURN)
    public Result<DiDiV5CollidingResultResponseDTO> colliding(String mediaName, DiDiV5CollidingRequestDTO requestDTO) {
        collidingUrl = collidingUrl.replace("mediaName", mediaName);
        HashMap<String, String> resMap = httpProxyClient.sendByCodeWithLog(requestDTO, collidingUrl, isProxy, MediaType.APPLICATION_JSON_VALUE,
                JSON.toJSONString(requestDTO), true, false);
        if (!"200".equals(resMap.get("httpcode")) || StringUtils.isBlank(resMap.get("content"))) {
            String errorMsg = "滴滴V5撞库接口异常-请求参数:" + JSON.toJSONString(requestDTO) + ";返回:" + JSON.toJSONString(resMap);
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.DIDI_V5_SERVICEERROR.getCode(), errorMsg));
            return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
        }
        DiDiV5CollidingResultResponseDTO responseDTO = JSON.parseObject(resMap.get("content"), DiDiV5CollidingResultResponseDTO.class);
        return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(responseDTO);
    }
}
