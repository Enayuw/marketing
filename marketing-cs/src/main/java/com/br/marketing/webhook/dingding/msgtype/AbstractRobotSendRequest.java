package com.br.marketing.webhook.dingding.msgtype;

/**
 * 机器人发送请求
 *
 * @author Guo Zeqiang
 * @dateTime 2023-08-17 13:47
 */
public abstract class AbstractRobotSendRequest {
    /**
     * 2023-08-17 13:56
     * 消息类型
     * 必填
     */
    private MsgType msgtype;

    public AbstractRobotSendRequest(MsgType msgtype) {
        this.msgtype = msgtype;
    }

    public AbstractRobotSendRequest() {
    }

    enum MsgType {
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

        private String name;

        MsgType(String name) {
            this.name = name;
        }

        MsgType() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return "MsgType{" +
                    "name='" + name + '\'' +
                    '}';
        }
    }
}
