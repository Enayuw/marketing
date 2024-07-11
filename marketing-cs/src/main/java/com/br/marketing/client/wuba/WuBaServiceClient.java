package com.br.marketing.client.wuba;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.cloud.web.MethodType;
import com.br.cloud.web.PrometheusTimeMethod;
import com.br.marketing.client.HttpProxyClient;
import com.br.marketing.client.wuba.input.WuBaSubmitDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.webhook.dingding.msgtype.DingDingMarkdownMessage;
import com.br.marketing.webhook.dingding.service.DingDingRobotHookService;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @PrometheusTimeMethod(buckets = {0.02d, 0.05d, 0.2d, 0.5d, 1d}, methodType = MethodType.REMOTE)
    public Result submitCredentialStuffingList(List<String> cells) {
        // 封装请求
        Map<String, Object> retMap = Maps.newHashMap();
        retMap.put("orgCode", orgCode);
        retMap.put("list", cells);
        HashMap<String, String> resMap;

        // 调用客户接口
        // todo 修改挡板
        if (marketingCommonConfig.getXieChengSmsCollidingRetrySwitch().get(0)) {
            resMap = getMock();
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
        // todo 修改挡板
        if (marketingCommonConfig.getXieChengSmsCollidingRetrySwitch().get(0)) {
            resMap = getMock();
        } else {
            resMap = httpProxyClient.sendByCodeWithLog(batchNo, queryCredentialStuffingResultUrl, isProxy,
                    MediaType.APPLICATION_JSON_UTF8_VALUE, JSON.toJSONString(batchNo), true, false);
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
        // todo 修改挡板
        if (marketingCommonConfig.getXieChengSmsCollidingRetrySwitch().get(0)) {
            resMap = getMock();
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
        // todo 修改挡板
        if (marketingCommonConfig.getXieChengSmsCollidingRetrySwitch().get(0)) {
            resMap = getMock();
        } else {
            resMap = httpProxyClient.sendByCodeWithLog(batchNo, queryConversionResultUrl, isProxy,
                    MediaType.APPLICATION_JSON_UTF8_VALUE, JSON.toJSONString(batchNo), true, false);
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

    private HashMap<String, String> getMock() {
        return new HashMap<>();
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
            dingDingRobotHookService.sendMessageGroup(token,
                    secret, dingDingMarkdownMessage, isProxy);
        } catch (Exception e) {
            log.error(text+" 发送钉钉消息失败:"+e.getMessage(),e);
        }
    }
}
