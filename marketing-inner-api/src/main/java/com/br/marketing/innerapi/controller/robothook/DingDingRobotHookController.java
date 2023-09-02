package com.br.marketing.innerapi.controller.robothook;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.webhook.dingding.msgtype.DingDingTextMessage;
import com.br.marketing.webhook.dingding.service.DingDingRobotHookService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 钉钉机器接口
 *
 * @author Guo Zeqiang
 * @dateTime 2023-09-02 13:30
 */
@RestController
@RequestMapping(value = "robot/dingding")
public class DingDingRobotHookController {

    @Resource
    private DingDingRobotHookService dingDingRobotHookService;

    /**
     * 2023-09-02 13:33
     * 发送文本消息
     *
     * @param accessToken         访问令牌 使用{@code webHook}时该字段可为空
     * @param secret              加签密钥 安全设置为非加签时可为空
     * @param dingDingTextMessage 文本消息
     */
    @PostMapping(path = {"text"})
    public ApiResult<String> sendTextMessage(@RequestParam String accessToken
            , @RequestParam(required = false) String secret
            , @RequestBody DingDingTextMessage dingDingTextMessage) {
        return dingDingRobotHookService.sendMessageGroup(accessToken, secret, dingDingTextMessage);
    }

    @PostMapping(path = {"webHook/text"})
    public ApiResult<String> sendTextMessageWebHook(@RequestParam String webHook
            , @RequestParam(required = false) String secret
            , @RequestBody DingDingTextMessage dingDingTextMessage) {
        return dingDingRobotHookService.sendMessageGroupWebHook(webHook, secret, dingDingTextMessage);
    }
}
