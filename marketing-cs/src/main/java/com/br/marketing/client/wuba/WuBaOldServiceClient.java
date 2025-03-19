package com.br.marketing.client.wuba;

import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.cloud.web.MethodType;
import com.br.cloud.web.PrometheusTimeMethod;
import com.br.common.log.AlertLog;
import com.br.marketing.client.HttpProxyClient;
import com.br.marketing.client.net.ApiCallerUtil;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.WubaCollidingDataLog;
import com.br.marketing.entity.WubaCollidingDataLogExample;
import com.br.marketing.mapper.datasource.log.InterfaceLogMapper;
import com.br.marketing.mapper.WubaCollidingDataLogMapper;
import com.br.marketing.mock.MockService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.webhook.dingding.msgtype.DingDingMarkdownMessage;
import com.br.marketing.webhook.dingding.service.DingDingRobotHookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * @Description WuBaServiceClient
 * @Author hong.chen
 * @CreateTime 2024/12/26
 */
@Slf4j
@Service
public class WuBaOldServiceClient {
    @Autowired
    HttpProxyClient httpProxyClient;
    @Resource
    private DingDingRobotHookService dingDingRobotHookService;
    @Autowired
    MarketingCommonConfig marketingCommonConfig;
    @Autowired
    WubaCollidingDataLogMapper wubaCollidingDataLogMapper;
    private static final Random RANDOM = new Random();
    @Resource
    private MockService mockService;

    @Qualifier("restTemplateByProxy")
    @Autowired
    RestTemplate restTemplateByProxy;

    @Qualifier("interfaceLogDbpool")
    @Autowired
    ThreadPoolExecutor interfaceLogDbpool;

    @Autowired
    InterfaceLogMapper interfaceLogMapper;


    @PrometheusTimeMethod(buckets = {0.02d, 0.05d, 0.2d, 0.5d, 1d}, methodType = MethodType.REMOTE)
    public Result queryCredentialStuffingResult(String batchNo) {
        HashMap<String, String> resMap;

        // 调用客户接口
        if(marketingCommonConfig.getWuBaOldMock().getBoolean("switch")){
            resMap = new HashMap<>();
            resMap.put("httpcode", "200");
            resMap.put("content", JSONObject.toJSONString(marketingCommonConfig.getWuBaOldMock().getJSONObject("mock")));
        } else {
            String queryCredentialStuffingResultUrl = marketingCommonConfig.getWuBaOldCollidingUrlConfig().getString(
                    "queryCredentialStuffingResultUrl");
            resMap = getWuBaServerQueryResult(queryCredentialStuffingResultUrl, batchNo);
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

    private HashMap<String, String> getWuBaServerQueryResult(String url, String batchNo) {
        String param = "batchNo=" + batchNo;
        String urlConcatParam = url + "?" + param;
        ResponseEntity<String> reponse = new ApiCallerUtil(restTemplateByProxy, interfaceLogMapper, interfaceLogDbpool)
                .setUrl(urlConcatParam).getReponse(JSON.toJSONString(param));

        HashMap<String, String> resMap = new HashMap<>();
        resMap.put("httpcode", String.valueOf(reponse.getStatusCodeValue()));
        resMap.put("content", reponse.getBody());
        return resMap;
    }

    private HashMap<String, String> getMock(String batchNo, HashMap<String, String> resMap) {
        HashMap<String, String> resMock = new HashMap<>();
        JSONObject content = JSONObject.parseObject(resMap.get("content"));
        if (Objects.equals(content.get("code"), "66666")) {
            if (StringUtils.isEmpty(batchNo)) {
                HashMap<String, Object> contentMock = new HashMap<>();
                contentMock.put("code", 0);
                contentMock.put("msg", "成功");
                String batchNoMock = "wuba_zk_test" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
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
                if (collidingDataLog.getId().intValue() % 2 == 1) {
                    JSONObject jsonObject = new JSONObject();
                    String randomNumber = RANDOM.ints(1, 10)
                            .limit(10).mapToObj(String::valueOf).collect(Collectors.joining()) + "0";
                    jsonObject.put("id", randomNumber);
                    jsonObject.put("mobileEncrypt", collidingDataLog.getCell());

                    List<Integer> radomStatus = JSON.parseArray(content.getString("radomStatus")).toJavaList(Integer.class);
                    radomStatus.add(null);
                    Integer status = RandomUtil.randomEle(radomStatus);
                    if (Objects.nonNull(status)) {
                        jsonObject.put("status", status);
                    }

                    List<String> randomUserType = JSON.parseArray(content.getString("randomUserType")).toJavaList(String.class);
                    randomUserType.add(null);
                    String userType = RandomUtil.randomEle(randomUserType);
                    if (Objects.nonNull(userType)) {
                        jsonObject.put("userType", userType);
                    }

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

    public void sendDingDingAlert(String title, String text) {
        DingDingMarkdownMessage.Markdown markdown = new DingDingMarkdownMessage.Markdown();
        markdown.setTitle(title);
        markdown.setText(text);
        DingDingMarkdownMessage dingDingMarkdownMessage = new DingDingMarkdownMessage();
        dingDingMarkdownMessage.setMarkdown(markdown);

        String token = marketingCommonConfig.getWuBaDingDingAccessToken();
        String secret = marketingCommonConfig.getWuBaDingDingSecret();
        try {
            dingDingRobotHookService.sendMessageGroup(token, secret, dingDingMarkdownMessage, true);
        } catch (Exception e) {
            String subject = text + ",发送钉钉消息失败";
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_WUBA.getCode(), e.getMessage()
                    , subject), e);
        }
    }
}