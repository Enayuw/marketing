package com.br.marketing.webhook.dingding.service;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.webhook.dingding.msgtype.AbstractRobotSendRequest;

import java.util.Map;

/**
 * 钉钉自定义机器人接入
 * <p>
 * 调用频率限制
 * 由于消息发送太频繁会严重影响群成员的使用体验，因此钉钉开放平台对自定义机器人发送消息的频率作出以下限制：
 * <p>
 * 每个机器人每分钟最多发送20条消息到群里，如果超过20条，会限流10分钟。
 *
 * @author Guo Zeqiang
 * @dateTime 2023-07-27 21:16
 */
public interface DingDingRobotHookService {

    /**
     * 系统事件同步到钉钉的聊天群
     * <p>
     * <p>
     * *注意
     * 如果你有大量发消息的场景（譬如系统监控报警）可以将这些信息进行整合，通过markdown消息以摘要的形式发送到群里。
     * <p>
     * 安全设置
     * 1. 自定义关键词
     * 最多可以设置10个关键词，消息中至少包含其中1个关键词才可以发送成功
     * 2. 加签
     * 3. IP地址（段）
     * 设定后，只有来自IP地址范围内的请求才会被正常处理。支持两种设置方式：IP地址和IP地址段，暂不支持IPv6地址白名单，格式如下。
     * 格式          说明
     * 1.1.1.1      开发者的出口公网IP地址（非局域网地址）
     * 1.1.1.0/24   用CIDR表示的一个网段
     *
     * @param accessToken      访问令牌
     * @param secret           加签密钥 安全设置为非加签时可为空
     * @param robotSendRequest 请求发送的消息
     * @return 响应
     * // 消息内容中不包含任何关键词
     * {
     * "errcode":310000,
     * "errmsg":"keywords not in content"
     * }
     * <p>
     * // timestamp 无效
     * {
     * "errcode":310000,
     * "errmsg":"invalid timestamp"
     * }
     * <p>
     * // 签名不匹配
     * {
     * "errcode":310000,
     * "errmsg":"sign not match"
     * }
     * <p>
     * // IP地址不在白名单
     * {
     * "errcode":310000,
     * "errmsg":"ip X.X.X.X not in whitelist"
     * }
     * @dateTime 2023-07-27 21:27
     */
    ApiResult<String> sendMessageGroup(String accessToken, String secret
            , AbstractRobotSendRequest robotSendRequest);

    /**
     * 2023-09-04 14:56
     * 是否使用代理
     */
    ApiResult<String> sendMessageGroup(String accessToken, String secret
            , AbstractRobotSendRequest robotSendRequest, boolean isProxy);

    /**
     * 系统事件同步到钉钉的聊天群一定义webHook
     *
     * @param webHook          Webhook地址 例如：https://oapi.dingtalk.com/robot/send?access_token=XXXXXX
     * @param secret           加签密钥 安全设置为非加签时可为空
     * @param robotSendRequest 请求发送的消息
     * @return 响应
     * // 消息内容中不包含任何关键词
     * {
     * "errcode":310000,
     * "errmsg":"keywords not in content"
     * }
     * <p>
     * // timestamp 无效
     * {
     * "errcode":310000,
     * "errmsg":"invalid timestamp"
     * }
     * <p>
     * // 签名不匹配
     * {
     * "errcode":310000,
     * "errmsg":"sign not match"
     * }
     * <p>
     * // IP地址不在白名单
     * {
     * "errcode":310000,
     * "errmsg":"ip X.X.X.X not in whitelist"
     */
    ApiResult<String> sendMessageGroupWebHook(String webHook, String secret
            , AbstractRobotSendRequest robotSendRequest);

    /**
     * 2023-09-04 14:56
     * 是否使用代理
     */
    ApiResult<String> sendMessageGroupWebHook(String webHook, String secret
            , AbstractRobotSendRequest robotSendRequest, boolean isProxy);



    /**
     * 2024-03-05 17:47
     * 发送钉钉文本消息
     * @param content 消息内容
     * @param sendMgsInfoMap speed配置见：dingDingWebHookInfo
     */
     void sendDingDingTextMessage(String content, Map<String, Object> sendMgsInfoMap);



    }
