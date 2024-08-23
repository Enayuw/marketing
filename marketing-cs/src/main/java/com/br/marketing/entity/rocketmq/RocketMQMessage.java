package com.br.marketing.entity.rocketmq;

import lombok.Data;

/**
 * RocketMQ消息封装类
 * @Author: yu.xia@brgroup.com
 * @Date: 2024-08-22
 */
@Data
public class RocketMQMessage {
    /**
     * 消息全局唯一标识
     * 预留
     */
    String id;
    /**
     * 原本的消息体
     */
    String message;
    /**
     * 消息手动重入队列的标记
     */
    String messageMark;
}
