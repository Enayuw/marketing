package com.br.marketing.client.wuba;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.cloud.web.MethodType;
import com.br.cloud.web.PrometheusTimeMethod;
import com.br.common.log.AlertLog;
import com.br.marketing.client.HttpProxyClient;
import com.br.marketing.client.net.ApiCallerUtil;
import com.br.marketing.client.wuba.input.WuBaSubmitDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.WubaCollidingDataLog;
import com.br.marketing.entity.WubaCollidingDataLogExample;
import com.br.marketing.enums.MockInterfaceCodeEnum;
import com.br.marketing.mapper.InterfaceLogMapper;
import com.br.marketing.mapper.WubaCollidingDataLogMapper;
import com.br.marketing.mock.MockService;
import com.br.marketing.mock.custom.wuba.WuBaMockService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.webhook.dingding.msgtype.DingDingMarkdownMessage;
import com.br.marketing.webhook.dingding.service.DingDingRobotHookService;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * @Description WuBaServiceClient
 * @Author hong.chen
 * @CreateTime 2024/07/10
 */
@Slf4j
@Service
public class WuBaServiceClient {
    @Value("${api.wuba.orgCode:00}")
    String orgCode;
    @Value("${api.wuba.submitCredentialStuffingListUrl:00}")
    String submitCredentialStuffingListUrl;
    @Value("${api.wuba.queryCredentialStuffingResultUrl:00}")
    String queryCredentialStuffingResultUrl;
    @Value("${api.wuba.submitConversionListUrl:00}")
    String submitConversionListUrl;
    @Value("${api.wuba.queryConversionResultUrl:00}")
    String queryConversionResultUrl;
    @Value("${api.wuba.isProxy:true}")
    Boolean isProxy;

    @Autowired
    HttpProxyClient httpProxyClient;
    @Resource
    private DingDingRobotHookService dingDingRobotHookService;
    @Autowired
    MarketingCommonConfig marketingCommonConfig;
    @Autowired
    WubaCollidingDataLogMapper wubaCollidingDataLogMapper;

    @Resource
    private MockService mockService;
    @Resource
    private WuBaMockService wuBaMockService;

    @Autowired
    RestTemplate restTemplate;

    @Qualifier("restTemplateByProxy")
    @Autowired
    RestTemplate restTemplateByProxy;

    @Qualifier("interfaceLogDbpool")
    @Autowired
    ThreadPoolExecutor interfaceLogDbpool;

    @Autowired
    InterfaceLogMapper interfaceLogMapper;

    @PrometheusTimeMethod(buckets = {0.02d, 0.05d, 0.2d, 0.5d, 1d}, methodType = MethodType.REMOTE)
    public Result submitCredentialStuffingList(List<String> cells) {
        // 封装请求
        Map<String, Object> retMap = Maps.newHashMap();
        retMap.put("orgCode", orgCode);
        retMap.put("list", cells);
        HashMap<String, String> resMap;

        // 调用客户接口
        if (mockService.checkMockSwitch(MockInterfaceCodeEnum.ITF_WUBA_01.getCode())) {
            resMap = mockService.getMockContent(MockInterfaceCodeEnum.ITF_WUBA_01.getCode());
            resMap = getMock(null, resMap);
        } else {
            resMap = httpProxyClient.sendByCodeWithLog(retMap, submitCredentialStuffingListUrl, isProxy,
                    MediaType.APPLICATION_JSON_UTF8_VALUE, JSON.toJSONString(cells), true, false);
        }

        // 处理响应
        if (!"200".equals(resMap.get("httpcode")) || StringUtils.isBlank(resMap.get("content"))) {
            return new Result().setCode(ResultCode.FAIL.getValue()).setDate(JSON.toJSONString(resMap));
        }

        String content = resMap.get("content");
        JSONObject resultJson = JSONObject.parseObject(content);
        Integer code = resultJson.getInteger("code");
        String data = resultJson.getString("data");
        if (code == 0 && StringUtils.isNotEmpty(data)) {
            return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(data);
        } else {
            return new Result().setCode(ResultCode.FAIL.getValue()).setDate(JSON.toJSONString(resMap));
        }
    }

    @PrometheusTimeMethod(buckets = {0.02d, 0.05d, 0.2d, 0.5d, 1d}, methodType = MethodType.REMOTE)
    public Result queryCredentialStuffingResult(String batchNo) {
        HashMap<String, String> resMap;

        // 调用客户接口
        if (mockService.checkMockSwitch(MockInterfaceCodeEnum.ITF_WUBA_02.getCode())) {
            resMap = mockService.getMockContent(MockInterfaceCodeEnum.ITF_WUBA_02.getCode());
            resMap = getMock(batchNo, resMap);
        } else {
            resMap = getWuBaServerQueryResult(batchNo);
        }

        // 处理响应
        if (!"200".equals(resMap.get("httpcode")) || StringUtils.isBlank(resMap.get("content"))) {
            return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue()).setDate(JSON.toJSONString(resMap));
        }

        String content = resMap.get("content");
        JSONObject resultJson = JSONObject.parseObject(content);
        Integer code = resultJson.getInteger("code");
        if (code == 0) {
            return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(resultJson.get("data"));
        } else if (code == 9991) {
            return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue()).setDate(JSON.toJSONString(resMap));
        } else {
            return new Result().setCode(ResultCode.FAIL.getValue()).setDate(JSON.toJSONString(resMap));
        }
    }

    @PrometheusTimeMethod(buckets = {0.02d, 0.05d, 0.2d, 0.5d, 1d}, methodType = MethodType.REMOTE)
    public Result submitConversionList(List<WuBaSubmitDTO> wuBaSubmitDTOS) {
        // 封装请求
        Map<String, Object> retMap = Maps.newHashMap();
        retMap.put("orgCode", orgCode);
        retMap.put("list", wuBaSubmitDTOS);
        HashMap<String, String> resMap;

        // 调用客户接口
        if (mockService.checkMockSwitch(MockInterfaceCodeEnum.ITF_WUBA_03.getCode())) {
            resMap = mockService.getMockContent(MockInterfaceCodeEnum.ITF_WUBA_03.getCode());
            resMap = wuBaMockService.getMock03(resMap);
        } else {
            resMap = httpProxyClient.sendByCodeWithLog(retMap, submitConversionListUrl, isProxy,
                    MediaType.APPLICATION_JSON_UTF8_VALUE, JSON.toJSONString(wuBaSubmitDTOS), true, false);
        }

        // 处理响应
        if (!"200".equals(resMap.get("httpcode")) || StringUtils.isBlank(resMap.get("content"))) {
            return new Result().setCode(ResultCode.FAIL.getValue()).setDate(JSON.toJSONString(resMap));
        }

        String content = resMap.get("content");
        JSONObject resultJson = JSONObject.parseObject(content);
        Integer code = resultJson.getInteger("code");
        String data = resultJson.getString("data");
        if (code == 0 && StringUtils.isNotEmpty(data)) {
            return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(data);
        } else {
            return new Result().setCode(ResultCode.FAIL.getValue()).setDate(JSON.toJSONString(resMap));
        }
    }

    @PrometheusTimeMethod(buckets = {0.02d, 0.05d, 0.2d, 0.5d, 1d}, methodType = MethodType.REMOTE)
    public Result queryConversionResult(String batchNo) {
        HashMap<String, String> resMap;

        // 调用客户接口
        if (mockService.checkMockSwitch(MockInterfaceCodeEnum.ITF_WUBA_04.getCode())) {
            resMap = mockService.getMockContent(MockInterfaceCodeEnum.ITF_WUBA_04.getCode());
            resMap = wuBaMockService.getMock04(batchNo, resMap);
        } else {
            resMap = getWuBaServerQueryResult(batchNo);
        }

        // 处理响应  {"httpcode":"200","content":{"code":"0","data":[]}}
        if (!"200".equals(resMap.get("httpcode")) || StringUtils.isBlank(resMap.get("content"))) {
            return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue()).setDate(JSON.toJSONString(resMap));
        }

        String content = resMap.get("content");
        JSONObject resultJson = JSONObject.parseObject(content);
        Integer code = resultJson.getInteger("code");
        if (code == 0) {
            return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(resultJson.get("data"));
        } else if (code == 9991) {
            return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue()).setDate(JSON.toJSONString(resMap));
        } else {
            return new Result().setCode(ResultCode.FAIL.getValue()).setDate(JSON.toJSONString(resMap));
        }
    }

    private HashMap<String, String> getWuBaServerQueryResult(String batchNo){
        RestTemplate restTemplateCall;
        if (isProxy) {
            restTemplateCall = restTemplateByProxy;
        } else {
            restTemplateCall = restTemplate;
        }

        String param = "batchNo=" + batchNo;
        String url = queryCredentialStuffingResultUrl + "?" + param;
        ResponseEntity<String> reponse = new ApiCallerUtil(restTemplateCall, interfaceLogMapper, interfaceLogDbpool)
                .setUrl(url).getReponse();

        HashMap<String, String> resMap = new HashMap<>();
        resMap.put("httpcode",String.valueOf(reponse.getStatusCodeValue()));
        resMap.put("content",reponse.getBody());
        return resMap;
    }

    public void sendDingDingAlert(String title, String text) {
        DingDingMarkdownMessage.Markdown markdown = new DingDingMarkdownMessage.Markdown();
        markdown.setTitle(title);
        markdown.setText(text);
        DingDingMarkdownMessage dingDingMarkdownMessage = new DingDingMarkdownMessage();
        dingDingMarkdownMessage.setMarkdown(markdown);

        String token = marketingCommonConfig.getQiFuDingDingAccessToken();
        String secret = marketingCommonConfig.getQiFuDingDingSecret();
        try {
            dingDingRobotHookService.sendMessageGroup(token, secret, dingDingMarkdownMessage, isProxy);
        } catch (Exception e) {
            String subject = text + ",发送钉钉消息失败";
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_WUBA.getCode(), e.getMessage()
                    , subject), e);
        }
    }

    private HashMap<String, String> getMock(String batchNo, HashMap<String, String> resMap) {
        HashMap<String, String> resMock = new HashMap<>();
        JSONObject content = JSONObject.parseObject(resMap.get("content"));
        if (Objects.equals(content.get("code"), "66666")) {
            if (StringUtils.isEmpty(batchNo)) {
                HashMap<String, Object> contentMock = new HashMap<>();
                contentMock.put("code", 0);
                contentMock.put("msg", "成功");
                String batchNoMock = "csl_bairongkj_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
                contentMock.put("data", batchNoMock);

                resMock.put("httpcode", "200");
                resMock.put("content", JSON.toJSONString(contentMock));
                return resMock;
            }

            WubaCollidingDataLogExample logExample = new WubaCollidingDataLogExample();
            logExample.createCriteria().andBatchNoEqualTo(batchNo).andIsDeletedEqualTo(0);
            List<WubaCollidingDataLog> logs = wubaCollidingDataLogMapper.selectByExample(logExample);

            JSONArray array = new JSONArray();
            for (WubaCollidingDataLog collidingDataLog : logs) {
                JSONObject jsonObject = new JSONObject();
                if (collidingDataLog.getId().intValue() % 2 == 1) {
                    String randomNumber = new Random().ints(1, 10)
                            .limit(10).mapToObj(String::valueOf).collect(Collectors.joining()) + "0";
                    jsonObject.put("id", randomNumber);
                    jsonObject.put("mobileEncrypt", collidingDataLog.getCell());
                    array.add(jsonObject);
                }
            }

            HashMap<String, Object> contentMock = new HashMap<>();
            contentMock.put("code", 0);
            contentMock.put("msg", "成功");
            contentMock.put("data", array);

            resMock.put("httpcode", "200");
            resMock.put("content", JSON.toJSONString(contentMock));
            return resMock;
        } else {
            return resMap;
        }
    }
}