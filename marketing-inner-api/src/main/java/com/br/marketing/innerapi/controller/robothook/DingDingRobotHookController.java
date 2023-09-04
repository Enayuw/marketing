package com.br.marketing.innerapi.controller.robothook;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.webhook.dingding.msgtype.*;
import com.br.marketing.webhook.dingding.service.DingDingRobotHookService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
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
@Api(value = "钉钉机器人接口", tags = "钉钉机器人", produces = "application/json", consumes = "application/json", protocols = "http")
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
    @ApiOperation(value = "发送文本消息", notes = "发送文本消息")
    @ApiImplicitParams({@ApiImplicitParam(name = "accessToken", value = "访问令牌", paramType = "query", dataType = "string")
            , @ApiImplicitParam(name = "secret", paramType = "query", dataType = "string")
    })
    @PostMapping(path = {"text"})
    public ApiResult<String> sendTextMessage(@RequestParam String accessToken
            , @RequestParam(required = false) String secret
            , @RequestBody DingDingTextMessage dingDingTextMessage) {
        return dingDingRobotHookService.sendMessageGroup(accessToken, secret, dingDingTextMessage);
    }

    /**
     * 使用webHook
     * 发送文本消息
     *
     * @param webHook             Webhook地址 例如：https://oapi.dingtalk.com/robot/send?access_token=XXXXXX
     * @param secret              加签密钥 安全设置为非加签时可为空
     * @param dingDingTextMessage 文本消息
     */
    @ApiOperation(value = "使用webHook发送文本消息", notes = "使用webHook发送文本消息")
    @ApiImplicitParams({@ApiImplicitParam(name = "webHook", value = "Webhook地址 例如：https://oapi.dingtalk.com/robot/send?access_token=XXXXXX", paramType = "query", dataType = "string")
            , @ApiImplicitParam(name = "secret", paramType = "query", dataType = "string")
    })
    @PostMapping(path = {"webHook/text"})
    public ApiResult<String> sendTextMessageWebHook(@RequestParam String webHook
            , @RequestParam(required = false) String secret
            , @RequestBody DingDingTextMessage dingDingTextMessage) {
        return dingDingRobotHookService.sendMessageGroupWebHook(webHook, secret, dingDingTextMessage);
    }

    /**
     * 2023-09-02 13:33
     * 发送markdown(markdown) 消息
     *
     * @param accessToken             访问令牌
     * @param secret                  加签密钥 安全设置为非加签时可为空
     * @param dingDingMarkdownMessage markdown(markdown) 消息
     */
    @ApiOperation(value = "发送markdown(markdown) 消息", notes = "发送markdown(markdown) 消息")
    @ApiImplicitParams({@ApiImplicitParam(name = "accessToken", value = "访问令牌", paramType = "query", dataType = "string")
            , @ApiImplicitParam(name = "secret", paramType = "query", dataType = "string")
    })
    @PostMapping(path = {"markdown"})
    public ApiResult<String> sendMarkdownMessage(@RequestParam String accessToken
            , @RequestParam(required = false) String secret
            , @RequestBody DingDingMarkdownMessage dingDingMarkdownMessage) {
        return dingDingRobotHookService.sendMessageGroup(accessToken, secret, dingDingMarkdownMessage);
    }

    /**
     * 使用webHook
     * 发送markdown(markdown) 消息
     *
     * @param webHook                 Webhook地址 例如：https://oapi.dingtalk.com/robot/send?access_token=XXXXXX
     * @param secret                  加签密钥 安全设置为非加签时可为空
     * @param dingDingMarkdownMessage markdown(markdown) 消息
     */
    @ApiOperation(value = "使用webHook发送markdown(markdown) 消息", notes = "使用webHook发送markdown(markdown) 消息")
    @ApiImplicitParams({@ApiImplicitParam(name = "webHook", value = "Webhook地址 例如：https://oapi.dingtalk.com/robot/send?access_token=XXXXXX", paramType = "query", dataType = "string")
            , @ApiImplicitParam(name = "secret", paramType = "query", dataType = "string")
    })
    @PostMapping(path = {"webHook/markdown"})
    public ApiResult<String> sendMarkdownMessageWebHook(@RequestParam String webHook
            , @RequestParam(required = false) String secret
            , @RequestBody DingDingMarkdownMessage dingDingMarkdownMessage) {
        return dingDingRobotHookService.sendMessageGroupWebHook(webHook, secret, dingDingMarkdownMessage);
    }

    /**
     * 2023-09-02 13:33
     * 发送链接 (link) 消息
     *
     * @param accessToken         访问令牌
     * @param secret              加签密钥 安全设置为非加签时可为空
     * @param dingDingLinkMessage 文本消息
     */
    @ApiOperation(value = "发送链接 (link) 消息", notes = "发送链接 (link) 消息")
    @ApiImplicitParams({@ApiImplicitParam(name = "accessToken", value = "访问令牌", paramType = "query", dataType = "string")
            , @ApiImplicitParam(name = "secret", paramType = "query", dataType = "string")
    })
    @PostMapping(path = {"link"})
    public ApiResult<String> sendLinkMessage(@RequestParam String accessToken
            , @RequestParam(required = false) String secret
            , @RequestBody DingDingLinkMessage dingDingLinkMessage) {
        return dingDingRobotHookService.sendMessageGroup(accessToken, secret, dingDingLinkMessage);
    }

    /**
     * 使用webHook
     * 发送链接 (link) 消息
     *
     * @param webHook             Webhook地址 例如：https://oapi.dingtalk.com/robot/send?access_token=XXXXXX
     * @param secret              加签密钥 安全设置为非加签时可为空
     * @param dingDingLinkMessage 文本消息
     */
    @ApiOperation(value = "使用webHook发送链接 (link) 消息", notes = "使用webHook发送链接 (link) 消息")
    @ApiImplicitParams({@ApiImplicitParam(name = "webHook", value = "Webhook地址 例如：https://oapi.dingtalk.com/robot/send?access_token=XXXXXX", paramType = "query", dataType = "string")
            , @ApiImplicitParam(name = "secret", paramType = "query", dataType = "string")
    })
    @PostMapping(path = {"webHook/link"})
    public ApiResult<String> sendLinkMessageWebHook(@RequestParam String webHook
            , @RequestParam(required = false) String secret
            , @RequestBody DingDingLinkMessage dingDingLinkMessage) {
        return dingDingRobotHookService.sendMessageGroupWebHook(webHook, secret, dingDingLinkMessage);
    }

    /**
     * 2023-09-02 13:33
     * 发送FeedCard 消息
     *
     * @param accessToken             访问令牌
     * @param secret                  加签密钥 安全设置为非加签时可为空
     * @param dingDingFeedCardMessage 文本消息
     */
    @ApiOperation(value = "发送FeedCard消息", notes = "发送FeedCard消息")
    @ApiImplicitParams({@ApiImplicitParam(name = "accessToken", value = "访问令牌", paramType = "query", dataType = "string")
            , @ApiImplicitParam(name = "secret", paramType = "query", dataType = "string")
    })
    @PostMapping(path = {"feedCard"})
    public ApiResult<String> sendFeedCardMessage(@RequestParam String accessToken
            , @RequestParam(required = false) String secret
            , @RequestBody DingDingFeedCardMessage dingDingFeedCardMessage) {
        return dingDingRobotHookService.sendMessageGroup(accessToken, secret, dingDingFeedCardMessage);
    }

    /**
     * 使用webHook
     * 发送FeedCard 消息
     *
     * @param webHook                 Webhook地址 例如：https://oapi.dingtalk.com/robot/send?access_token=XXXXXX
     * @param secret                  加签密钥 安全设置为非加签时可为空
     * @param dingDingFeedCardMessage 文本消息
     */
    @ApiOperation(value = "使用webHook发送FeedCard消息", notes = "使用webHook发送FeedCard消息")
    @ApiImplicitParams({@ApiImplicitParam(name = "webHook", value = "Webhook地址 例如：https://oapi.dingtalk.com/robot/send?access_token=XXXXXX", paramType = "query", dataType = "string")
            , @ApiImplicitParam(name = "secret", paramType = "query", dataType = "string")
    })
    @PostMapping(path = {"webHook/feedCard"})
    public ApiResult<String> sendFeedCardMessageWebHook(@RequestParam String webHook
            , @RequestParam(required = false) String secret
            , @RequestBody DingDingFeedCardMessage dingDingFeedCardMessage) {
        return dingDingRobotHookService.sendMessageGroupWebHook(webHook, secret, dingDingFeedCardMessage);
    }

    /**
     * 2023-09-02 13:33
     * 发送ActionCard 消息
     *
     * @param accessToken               访问令牌
     * @param secret                    加签密钥 安全设置为非加签时可为空
     * @param dingDingActionCardMessage 文本消息
     */
    @ApiOperation(value = "发送ActionCard消息", notes = "发送ActionCard消息")
    @ApiImplicitParams({@ApiImplicitParam(name = "accessToken", value = "访问令牌", paramType = "query", dataType = "string")
            , @ApiImplicitParam(name = "secret", paramType = "query", dataType = "string")
    })
    @PostMapping(path = {"actionCard"})
    public ApiResult<String> sendActionCardMessage(@RequestParam String accessToken
            , @RequestParam(required = false) String secret
            , @RequestBody DingDingActionCardMessage dingDingActionCardMessage) {
        return dingDingRobotHookService.sendMessageGroup(accessToken, secret, dingDingActionCardMessage);
    }

    /**
     * 使用webHook
     *
     * @param webHook                   Webhook地址 例如：https://oapi.dingtalk.com/robot/send?access_token=XXXXXX
     * @param secret                    加签密钥 安全设置为非加签时可为空
     * @param dingDingActionCardMessage 文本消息
     */
    @ApiOperation(value = "使用webHook发送ActionCard消息", notes = "发送ActionCard消息")
    @ApiImplicitParams({@ApiImplicitParam(name = "webHook", value = "Webhook地址 例如：https://oapi.dingtalk.com/robot/send?access_token=XXXXXX", paramType = "query", dataType = "string")
            , @ApiImplicitParam(name = "secret", paramType = "query", dataType = "string")
    })
    @PostMapping(path = {"webHook/actionCard"})
    public ApiResult<String> sendActionCardMessageWebHook(@RequestParam String webHook
            , @RequestParam(required = false) String secret
            , @RequestBody DingDingActionCardMessage dingDingActionCardMessage) {
        return dingDingRobotHookService.sendMessageGroupWebHook(webHook, secret, dingDingActionCardMessage);
    }
}
