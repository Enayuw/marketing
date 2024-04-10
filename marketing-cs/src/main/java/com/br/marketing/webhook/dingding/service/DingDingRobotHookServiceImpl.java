package com.br.marketing.webhook.dingding.service;

import com.alibaba.fastjson.JSONArray;
import com.br.marketing.client.HttpProxyClient;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.enums.ServiceResultEnum;
import com.br.marketing.webhook.dingding.msgtype.AbstractRobotSendRequest;
import com.br.marketing.webhook.dingding.msgtype.At;
import com.br.marketing.webhook.dingding.msgtype.DingDingTextMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

/**
 * 钉钉自定义机器人发送消息
 *
 * @author Guo Zeqiang
 * @dateTime 2023-08-17 19:27
 */
@Service
@Slf4j
public class DingDingRobotHookServiceImpl implements DingDingRobotHookService {


    @Resource
    private HttpProxyClient httpProxyClient;

    private static final String DINGDING_ROBOT_URL = "https://oapi.dingtalk.com/robot/send";

    @Override
    public ApiResult<String> sendMessageGroup(String accessToken, String secret
            , AbstractRobotSendRequest robotSendRequest, boolean isProxy) {
        ApiResult<String> apiResult = new ApiResult<>();
        String webHook;
        if (StringUtils.isNotBlank(accessToken)) {
            webHook = DINGDING_ROBOT_URL.concat("?access_token=").concat(accessToken);
        } else {
            apiResult.fail("访问令牌,不可为空");
            return apiResult;
        }
        sendWebHook(webHook, secret, robotSendRequest, apiResult, isProxy);
        return apiResult;
    }

    @Override
    public ApiResult<String> sendMessageGroup(String accessToken, String secret
            , AbstractRobotSendRequest robotSendRequest) {
        return sendMessageGroup(accessToken, secret, robotSendRequest, true);
    }

    @Override
    public ApiResult<String> sendMessageGroupWebHook(String webHook, String secret
            , AbstractRobotSendRequest robotSendRequest) {
        return sendMessageGroup(webHook, secret, robotSendRequest, true);
    }

    @Override
    public ApiResult<String> sendMessageGroupWebHook(String webHook
            , String secret
            , AbstractRobotSendRequest robotSendRequest
            , boolean isProxy) {
        ApiResult<String> apiResult = new ApiResult<>();
        if (StringUtils.isBlank(webHook)) {
            apiResult.fail("webHook(web地址),不可为空");
            return apiResult;
        }
        sendWebHook(webHook, secret, robotSendRequest, apiResult, isProxy);
        return apiResult;
    }

    /**
     * 2023-08-21 13:50
     * 生成签名
     */
    private String createSign(Long timestamp, String secret)
            throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeyException {
        // 产生随机密钥
        KeyGenerator.getInstance("HmacSHA256");
        String stringToSign = timestamp + "\n" + secret;
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] signData = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
        return URLEncoder.encode(new String(Base64.encodeBase64(signData)), StandardCharsets.UTF_8.toString());
    }

    /**
     * 2023-09-02 17:14
     * 发送消息到web地址
     */
    private void sendWebHook(String webHook
            , String secret
            , AbstractRobotSendRequest robotSendRequest
            , ApiResult<String> apiResult
            , boolean isProxy) {
        if (StringUtils.isNotBlank(secret)) {
            Long timestamp = System.currentTimeMillis();
            try {
                String sign = createSign(timestamp, secret);
                webHook = webHook + "&timestamp=" + timestamp + "&sign=" + sign;
            } catch (UnsupportedEncodingException | NoSuchAlgorithmException | InvalidKeyException e) {
                log.error(e.getMessage(), e);
                apiResult.fail(e.getMessage());
                return;
            }
        }
        HashMap<String, String> response = httpProxyClient.sendByCode(robotSendRequest
                , webHook
                , isProxy
                , MediaType.APPLICATION_JSON_UTF8_VALUE, null);
        String key = "httpcode";
        String httpCode = response.get(key);
        String httpCodeStart5 = "5";
        String httpCodeStart4 = "4";
        if (httpCode.contains(httpCodeStart5) || httpCode.contains(httpCodeStart4)) {
            apiResult.fail("访问地址错误或服务端异常！httpcode:" + httpCode);
            log.error(apiResult.getMessage());
            return;
        }
        apiResult.success(response.get("content"), ServiceResultEnum.SUCCESS);
    }


    /**
     * 2024-03-05 17:47
     * 发送钉钉文本消息
     * @param content 消息内容
     * @param sendMgsInfoMap speed配置见：dingDingWebHookInfo
     */
    @Override
    public void sendDingDingTextMessage(String content, Map<String, Object> sendMgsInfoMap) {
        DingDingTextMessage dingDingTextMessage = new DingDingTextMessage();
        DingDingTextMessage.Text text = new DingDingTextMessage.Text();
        dingDingTextMessage.setText(text);
        JSONArray ats = (JSONArray) sendMgsInfoMap.get("at");
        if (ats != null) {
            At at = new At();
            at.setAtMobiles(ats.toJavaList(String.class));
            dingDingTextMessage.setAt(at);
        }
        text.setContent(content);
        log.warn(dingDingTextMessage.toString());
        // 发送实时消息
        sendMessageGroup(sendMgsInfoMap.get("token").toString()
                , sendMgsInfoMap.get("secret").toString()
                , dingDingTextMessage);
    }
}
