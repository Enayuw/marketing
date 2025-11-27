package com.br.marketing.innerapi.controller.robothook;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.webhook.dingding.msgtype.*;
import com.br.marketing.webhook.dingding.service.DingDingRobotHookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "钉钉机器人接口", description = "钉钉机器人")
public class DingDingRobotHookController {

    @Resource
    private DingDingRobotHookService dingDingRobotHookService;

    /**
     * 2023-09-02 13:33
     * 发送文本消息
     *
     * @param accessToken         访问令牌
     * @param secret              加签密钥 安全设置为非加签时可为空
     * @param dingDingTextMessage 文本消息
     */
    @Operation(summary = "发送文本消息", description = "发送文本消息")
    @Parameters({@Parameter(name = "accessToken", required = true
            , description = "访问令牌")
            , @Parameter(name = "secret"
            , description = "加签密钥")
            , @Parameter(name = "isProxy"
            , description = "是否使用代理，可不使用，默认为true")
    })
    @PostMapping(path = {"text"})
    public ApiResult<String> sendTextMessage(@RequestParam String accessToken
            , @RequestParam(required = false) String secret
            , @RequestParam(required = false, defaultValue = "true") Boolean isProxy
            , @RequestBody DingDingTextMessage dingDingTextMessage) {
        return dingDingRobotHookService.sendMessageGroup(accessToken, secret, dingDingTextMessage
                , isProxy == null || isProxy);
    }

    /**
     * 使用webHook
     * 发送文本消息
     *
     * @param webHook             Webhook地址 例如：https://oapi.dingtalk.com/robot/send?access_token=XXXXXX
     * @param secret              加签密钥 安全设置为非加签时可为空
     * @param dingDingTextMessage 文本消息
     */
    @Operation(summary = "使用webHook发送文本消息", description = "使用webHook发送文本消息")
    @Parameters({@Parameter(name = "webHook", required = true
            , description = "Webhook地址 例如：https://oapi.dingtalk.com/robot/send?access_token=XXXXXX")
            , @Parameter(name = "secret"
            , description = "加签密钥")
            , @Parameter(name = "isProxy"
            , description = "是否使用代理，可不使用，默认为true")
    })
    @PostMapping(path = {"webHook/text"})
    public ApiResult<String> sendTextMessageWebHook(@RequestParam String webHook
            , @RequestParam(required = false) String secret
            , @RequestParam(required = false, defaultValue = "true") Boolean isProxy
            , @RequestBody DingDingTextMessage dingDingTextMessage) {
        return dingDingRobotHookService.sendMessageGroupWebHook(webHook, secret, dingDingTextMessage
                , isProxy == null || isProxy);
    }

    /**
     * 2023-09-02 13:33
     * 发送markdown(markdown) 消息
     *
     * @param accessToken             访问令牌
     * @param secret                  加签密钥 安全设置为非加签时可为空
     * @param dingDingMarkdownMessage markdown(markdown) 消息
     */
    @Operation(summary = "发送markdown(markdown) 消息", description = "发送markdown(markdown) 消息")
    @Parameters({@Parameter(name = "accessToken", required = true
            , description = "访问令牌")
            , @Parameter(name = "secret"
            , description = "加签密钥")
            , @Parameter(name = "isProxy"
            , description = "是否使用代理，可不使用，默认为true")
    })
    @PostMapping(path = {"markdown"})
    public ApiResult<String> sendMarkdownMessage(@RequestParam String accessToken
            , @RequestParam(required = false) String secret
            , @RequestParam(required = false, defaultValue = "true") Boolean isProxy
            , @RequestBody DingDingMarkdownMessage dingDingMarkdownMessage) {
        return dingDingRobotHookService.sendMessageGroup(accessToken, secret, dingDingMarkdownMessage
                , isProxy == null || isProxy);
    }

    /**
     * 使用webHook
     * 发送markdown(markdown) 消息
     *
     * @param webHook                 Webhook地址 例如：https://oapi.dingtalk.com/robot/send?access_token=XXXXXX
     * @param secret                  加签密钥 安全设置为非加签时可为空
     * @param dingDingMarkdownMessage markdown(markdown) 消息
     */
    @Operation(summary = "使用webHook发送markdown(markdown) 消息", description = "使用webHook发送markdown(markdown) 消息")
    @Parameters({@Parameter(name = "webHook", required = true
            , description = "Webhook地址 例如：https://oapi.dingtalk.com/robot/send?access_token=XXXXXX")
            , @Parameter(name = "secret"
            , description = "加签密钥")
            , @Parameter(name = "isProxy"
            , description = "是否使用代理，可不使用，默认为true")
    })
    @PostMapping(path = {"webHook/markdown"})
    public ApiResult<String> sendMarkdownMessageWebHook(@RequestParam String webHook
            , @RequestParam(required = false) String secret
            , @RequestParam(required = false, defaultValue = "true") Boolean isProxy
            , @RequestBody DingDingMarkdownMessage dingDingMarkdownMessage) {
        return dingDingRobotHookService.sendMessageGroupWebHook(webHook, secret, dingDingMarkdownMessage
                , isProxy == null || isProxy);
    }

    /**
     * 2023-09-02 13:33
     * 发送链接 (link) 消息
     *
     * @param accessToken         访问令牌
     * @param secret              加签密钥 安全设置为非加签时可为空
     * @param dingDingLinkMessage 文本消息
     */
    @Operation(summary = "发送链接 (link) 消息", description = "发送链接 (link) 消息")
    @Parameters({@Parameter(name = "accessToken", required = true
            , description = "访问令牌")
            , @Parameter(name = "secret"
            , description = "加签密钥")
            , @Parameter(name = "isProxy"
            , description = "是否使用代理，可不使用，默认为true")
    })
    @PostMapping(path = {"link"})
    public ApiResult<String> sendLinkMessage(@RequestParam String accessToken
            , @RequestParam(required = false) String secret
            , @RequestParam(required = false, defaultValue = "true") Boolean isProxy
            , @RequestBody DingDingLinkMessage dingDingLinkMessage) {
        return dingDingRobotHookService.sendMessageGroup(accessToken, secret, dingDingLinkMessage
                , isProxy == null || isProxy);
    }

    /**
     * 使用webHook
     * 发送链接 (link) 消息
     *
     * @param webHook             Webhook地址 例如：https://oapi.dingtalk.com/robot/send?access_token=XXXXXX
     * @param secret              加签密钥 安全设置为非加签时可为空
     * @param dingDingLinkMessage 文本消息
     */
    @Operation(summary = "使用webHook发送链接 (link) 消息", description = "使用webHook发送链接 (link) 消息")
    @Parameters({@Parameter(name = "webHook", required = true
            , description = "Webhook地址 例如：https://oapi.dingtalk.com/robot/send?access_token=XXXXXX")
            , @Parameter(name = "secret"
            , description = "加签密钥")
            , @Parameter(name = "isProxy"
            , description = "是否使用代理，可不使用，默认为true")
    })
    @PostMapping(path = {"webHook/link"})
    public ApiResult<String> sendLinkMessageWebHook(@RequestParam String webHook
            , @RequestParam(required = false) String secret
            , @RequestParam(required = false, defaultValue = "true") Boolean isProxy
            , @RequestBody DingDingLinkMessage dingDingLinkMessage) {
        return dingDingRobotHookService.sendMessageGroupWebHook(webHook, secret, dingDingLinkMessage
                , isProxy == null || isProxy);
    }

    /**
     * 2023-09-02 13:33
     * 发送FeedCard 消息
     *
     * @param accessToken             访问令牌
     * @param secret                  加签密钥 安全设置为非加签时可为空
     * @param dingDingFeedCardMessage 文本消息
     */
    @Operation(summary = "发送FeedCard消息", description = "发送FeedCard消息")
    @Parameters({@Parameter(name = "accessToken", required = true
            , description = "访问令牌")
            , @Parameter(name = "secret"
            , description = "加签密钥")
            , @Parameter(name = "isProxy"
            , description = "是否使用代理，可不使用，默认为true")
    })
    @PostMapping(path = {"feedCard"})
    public ApiResult<String> sendFeedCardMessage(@RequestParam String accessToken
            , @RequestParam(required = false) String secret
            , @RequestParam(required = false, defaultValue = "true") Boolean isProxy
            , @RequestBody DingDingFeedCardMessage dingDingFeedCardMessage) {
        return dingDingRobotHookService.sendMessageGroup(accessToken, secret, dingDingFeedCardMessage
                , isProxy == null || isProxy);
    }

    /**
     * 使用webHook
     * 发送FeedCard 消息
     *
     * @param webHook                 Webhook地址 例如：https://oapi.dingtalk.com/robot/send?access_token=XXXXXX
     * @param secret                  加签密钥 安全设置为非加签时可为空
     * @param dingDingFeedCardMessage 文本消息
     */
    @Operation(summary = "使用webHook发送FeedCard消息", description = "使用webHook发送FeedCard消息")
    @Parameters({@Parameter(name = "webHook", required = true
            , description = "Webhook地址 例如：https://oapi.dingtalk.com/robot/send?access_token=XXXXXX")
            , @Parameter(name = "secret"
            , description = "加签密钥")
            , @Parameter(name = "isProxy"
            , description = "是否使用代理，可不使用，默认为true")
    })
    @PostMapping(path = {"webHook/feedCard"})
    public ApiResult<String> sendFeedCardMessageWebHook(@RequestParam String webHook
            , @RequestParam(required = false) String secret
            , @RequestParam(required = false, defaultValue = "true") Boolean isProxy
            , @RequestBody DingDingFeedCardMessage dingDingFeedCardMessage) {
        return dingDingRobotHookService.sendMessageGroupWebHook(webHook, secret, dingDingFeedCardMessage
                , isProxy == null || isProxy);
    }

    /**
     * 2023-09-02 13:33
     * 发送ActionCard 消息
     *
     * @param accessToken               访问令牌
     * @param secret                    加签密钥 安全设置为非加签时可为空
     * @param dingDingActionCardMessage 文本消息
     */
    @Operation(summary = "发送ActionCard消息", description = "发送ActionCard消息")
    @Parameters({@Parameter(name = "accessToken", required = true
            , description = "访问令牌")
            , @Parameter(name = "secret"
            , description = "加签密钥")
            , @Parameter(name = "isProxy"
            , description = "是否使用代理，可不使用，默认为true")
    })
    @PostMapping(path = {"actionCard"})
    public ApiResult<String> sendActionCardMessage(@RequestParam String accessToken
            , @RequestParam(required = false) String secret
            , @RequestParam(required = false, defaultValue = "true") Boolean isProxy
            , @RequestBody DingDingActionCardMessage dingDingActionCardMessage) {
        return dingDingRobotHookService.sendMessageGroup(accessToken, secret, dingDingActionCardMessage
                , isProxy == null || isProxy);
    }

    /**
     * 使用webHook
     *
     * @param webHook                   Webhook地址 例如：https://oapi.dingtalk.com/robot/send?access_token=XXXXXX
     * @param secret                    加签密钥 安全设置为非加签时可为空
     * @param dingDingActionCardMessage 文本消息
     */
    @Operation(summary = "使用webHook发送ActionCard消息", description = "发送ActionCard消息")
    @Parameters({@Parameter(name = "webHook", required = true
            , description = "Webhook地址 例如：https://oapi.dingtalk.com/robot/send?access_token=XXXXXX")
            , @Parameter(name = "secret"
            , description = "加签密钥")
            , @Parameter(name = "isProxy"
            , description = "是否使用代理，可不使用，默认为true")
    })
    @PostMapping(path = {"webHook/actionCard"})
    public ApiResult<String> sendActionCardMessageWebHook(@RequestParam String webHook
            , @RequestParam(required = false) String secret
            , @RequestParam(required = false, defaultValue = "true") Boolean isProxy
            , @RequestBody DingDingActionCardMessage dingDingActionCardMessage) {
        return dingDingRobotHookService.sendMessageGroupWebHook(webHook, secret, dingDingActionCardMessage
                , isProxy == null || isProxy);
    }
}
