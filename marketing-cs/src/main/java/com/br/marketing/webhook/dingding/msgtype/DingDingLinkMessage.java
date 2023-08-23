package com.br.marketing.webhook.dingding.msgtype;

/**
 * 链接 (link) 消息
 *
 * @author Guo Zeqiang
 * @dateTime 2023-08-17 10:28
 */
public class DingDingLinkMessage extends AbstractRobotSendRequest {
    /**
     * 2023-08-17 17:51
     * 连接
     */
    private Link link;


    public DingDingLinkMessage(Link link) {
        super(MsgType.LINK);
        this.link = link;
    }

    public Link getLink() {
        return link;
    }

    public void setLink(Link link) {
        this.link = link;
    }

    @Override
    public String toString() {
        return "DingDingLinkMessage{" +
                "link=" + link +
                '}';
    }

    public static class Link {
        /**
         * 2023-08-17 17:48
         * 消息标题
         * 必填
         */
        private String title;

        /**
         * 2023-08-17 17:49
         * 消息内容。如果太长只会部分展示。
         * 必填
         */
        private String text;

        /**
         * 2023-08-17 17:49
         * 点击消息跳转的URL
         * 必填
         */
        private String messageUrl;

        /**
         * 2023-08-17 17:47
         * 图片URL
         * 非必填
         */
        private String picUrl;

        public Link(String title, String text, String messageUrl) {
            this.title = title;
            this.text = text;
            this.messageUrl = messageUrl;
        }

        public Link(String title, String text, String messageUrl, String picUrl) {
            this.title = title;
            this.text = text;
            this.messageUrl = messageUrl;
            this.picUrl = picUrl;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getMessageUrl() {
            return messageUrl;
        }

        public void setMessageUrl(String messageUrl) {
            this.messageUrl = messageUrl;
        }

        public String getPicUrl() {
            return picUrl;
        }

        public void setPicUrl(String picUrl) {
            this.picUrl = picUrl;
        }

        @Override
        public String toString() {
            return "Link{" +
                    "title='" + title + '\'' +
                    ", text='" + text + '\'' +
                    ", messageUrl='" + messageUrl + '\'' +
                    ", picUrl='" + picUrl + '\'' +
                    '}';
        }
    }
}
