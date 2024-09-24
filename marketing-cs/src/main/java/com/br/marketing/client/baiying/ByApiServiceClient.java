package com.br.marketing.client.baiying;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.HttpProxyClient;
import com.br.marketing.client.baiying.input.ReqBlacklistDTO;
import com.br.marketing.client.biocloo.input.BlackDataDTO;
import com.br.marketing.client.biocloo.input.BlackDataRequestDTO;
import com.br.marketing.client.biocloo.utils.AESUtil;
import com.br.marketing.client.robotaiapi.input.TransferJsonDataDTO;
import com.br.marketing.client.robotaiapi.input.TransferRobotOutboundDTO;
import com.br.marketing.client.robotaiapi.input.TransferRobotOutboundSoleDTO;
import com.br.marketing.client.robotaiapi.output.TransferRobotDataVO;
import com.br.marketing.client.robotaiapi.output.TransferRobotOutboundVO;
import com.br.marketing.common.annoation.DistributeLog;
import com.br.marketing.common.annoation.RetryMethod;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.DataDistributeDetailLog;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import com.google.common.base.Joiner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

/**
 * @ClassName ByApiServiceClient
 * @Description 百应黑名单接口
 * @Author kongbx
 * @Date 2024/5/27 11:31
 */
@Service
@Slf4j
public class ByApiServiceClient {

    @Autowired
    RestTemplate restTemplate;

    @Value("${api.zhongAn.isProxy:false}")
    Boolean isProxy;

    @Value("${api.biocloo.isProxy:true}")
    private Boolean isBioclooProxy;

    @Value(value = "${api.baiying.postBlackList:00}")
    private String pushBlackDataUrl;

    @Value(value = "${api.biocloo.blackList:'https://callcenter.biocloo.com.cn/api/middle/robotai/v1/process_data'}")
    private String blackListUrl;

    @Autowired
    HttpProxyClient httpProxyClient;

    @Autowired
    MarketingCommonConfig marketingCommonConfig;

    private final static String TITLE = "【推送百应数据】";

    private final static String TITLE_BAIKELU = "【推送百可录数据】";

    @RetryMethod(retryNowNum = 3,isOrNoDbRetry = true)
    public Result pushBaiying(ReqBlacklistDTO dto, Integer retry){

        HashMap<String, String> resMap = new HashMap<>();
        // 获取挡板开关
        HashMap<String, Object> mock = marketingCommonConfig.getBaiYingUndoMock();
        if (mock.get("switch") == Boolean.TRUE) {
            JSONObject mockJson = new JSONObject();
            mockJson.put("code", mock.get("code"));
            mockJson.put("message", "处理成功");
            resMap.put("content", JSON.toJSONString(mockJson));
            resMap.put("httpcode", mock.get("httpcode").toString());
        } else {
            long start = System.currentTimeMillis();
            log.warn(TITLE+"调度开始, requestParam{}", JSONObject.toJSONString(dto));
            resMap = httpProxyClient.sendByCodeWithLog(dto, pushBlackDataUrl, isProxy,
                    MediaType.APPLICATION_JSON_UTF8_VALUE,
                    JSON.toJSONString(dto), true, true);
            long end = System.currentTimeMillis();
            log.warn(TITLE+"调度结束, result:{}, 耗时:{}", resMap, end - start);
        }

        if (!"200".equals(resMap.get("httpcode")) || StringUtils.isBlank(resMap.get("content"))) {
            log.error("调用百应【黑名单】接口异常-请求参数:{};返回:{}", JSON.toJSONString(dto), JSON.toJSONString(resMap));
            return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue()).setMessage(JSON.toJSONString(resMap));
        }

        String content = resMap.get("content");
        JSONObject resultJson = JSONObject.parseObject(content);
        String code = resultJson.getString("code");

        if ("000000".equals(code)) {
            log.warn("调用百应【黑名单】接口，返回code为000000，请求正常");
            return new Result().setCode(ResultCode.SUCCESS.getValue()).setMessage(content);
        }else {
            log.error("调用百应【黑名单】接口异常，返回code非000000，最多重试三次");
            return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
        }
    }

    @RetryMethod(retryNowNum = 3,isOrNoDbRetry = true)
    public Result pushDataToBiocloo(ReqBlacklistDTO dto, Integer retry){

        Integer retry1 = retry;
        if (CollectionUtil.isEmpty(dto.getData())) {
            log.warn("推送百可录去重后推送数据为0");
            return new Result().setCode(ResultCode.SUCCESS.getValue());
        }
        JSONObject shuHeToBioclooAesKeyConfig = marketingCommonConfig.getShuHeToBioclooAesKeyConfig();
        String encryptData = AESUtil.encryptToBase64(JSONObject.toJSONString(dto), shuHeToBioclooAesKeyConfig.getString(dto.getApiCode()));
        BlackDataRequestDTO requestDTO = new BlackDataRequestDTO();
        requestDTO.setApiCode(dto.getApiCode());
        requestDTO.setJsonData(encryptData);
        long start = System.currentTimeMillis();
        log.warn(TITLE_BAIKELU + "调度开始, requestParam{}", JSONObject.toJSONString(dto));
        HashMap<String, String> resMap  = httpProxyClient.sendByCodeWithLog(requestDTO, blackListUrl, isBioclooProxy,
                MediaType.APPLICATION_FORM_URLENCODED_VALUE, JSONObject.toJSONString(dto), true, true);
        long end = System.currentTimeMillis();
        log.warn(TITLE_BAIKELU + "调度结束, result:{}, 耗时:{}", resMap, end - start);

        if (!"200".equals(resMap.get("httpcode")) || StringUtils.isBlank(resMap.get("content"))) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YIXIN_INTERFACEERROR.getCode(),
                    String.format("请求参数:%s,返回:%s", JSON.toJSONString(dto), JSON.toJSONString(resMap)), "调用百可录【黑名单】接口异常"));
            return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue()).setMessage(JSON.toJSONString(resMap));
        }
        String content = resMap.get("content");
        JSONObject resultJson = JSONObject.parseObject(content);
        String code = resultJson.getString("code");
        if ("000000".equals(code)) {
            log.warn("调用百可录【黑名单】接口，返回code为000000，请求正常");
            return new Result().setCode(ResultCode.SUCCESS.getValue()).setMessage(content);
        } else {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YIXIN_INTERFACEERROR.getCode(), "返回code非000000，最多重试三次",
                    "调用百可录【黑名单】接口异常"));
            return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
        }
    }
}
