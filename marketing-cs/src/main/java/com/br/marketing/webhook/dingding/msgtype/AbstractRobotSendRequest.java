package com.br.marketing.webhook.dingding.msgtype;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

/**
 * 机器人发送请求
 *
 * @author Guo Zeqiang
 * @dateTime 2023-08-17 13:47
 */
public abstract class AbstractRobotSendRequest implements Serializable {
    /**
     * 2023-08-17 13:56
     * 消息类型
     * 必填
     */
    @Schema(description = "文本消息", hidden = true)
    private String msgtype;

    public AbstractRobotSendRequest(MsgType msgtype) {
        this.msgtype = msgtype.getValue();
    }

    public String getMsgtype() {
        return msgtype;
    }

    @Override
    public String toString() {
        return "AbstractRobotSendRequest{" +
                "msgtype=" + msgtype +
                '}';
    }

    protected enum MsgType {
        /**
         * 2023-08-17 17:17
         * 文本
         */
        TEXT("text"),
        /**
         * 2023-08-17 17:17
         * 连接
         */
        LINK("link"),
        /**
         * 2023-08-17 17:17
         * markdown
         */
        MARKDOWN("markdown"),
        /**
         * 2023-08-17 17:17
         * ActionCard
         * 独立跳转ActionCard类型
         */
        ACTION_CARD("actionCard"),
        /**
         * 2023-08-17 17:17
         * FeedCard
         */
        FEED_CARD("feedCard");

        private String value;

        MsgType(String value) {
            this.value = value;
        }

        MsgType() {
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return "MsgType{" +
                    "value='" + value + '\'' +
                    '}';
        }
    }
}
