package com.br.marketing.webhook.dingding.msgtype;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.util.List;

/**
 * FeedCard 消息
 *
 * @author Guo Zeqiang
 * @dateTime 2023-08-17 10:28
 */
@Schema(description = "FeedCard 消息")
public class DingDingFeedCardMessage extends AbstractRobotSendRequest {

    private static final long serialVersionUID = 2930193924823528243L;
    /**
     * 2023-08-17 19:23
     * FeedCard
     */
    @Schema(description = "FeedCard")
    private FeedCard feedCard;

    public DingDingFeedCardMessage() {
        super(MsgType.FEED_CARD);
    }

    public DingDingFeedCardMessage(FeedCard feedCard) {
        super(MsgType.FEED_CARD);
        this.feedCard = feedCard;
    }

    public FeedCard getFeedCard() {
        return feedCard;
    }

    public void setFeedCard(FeedCard feedCard) {
        this.feedCard = feedCard;
    }

    @Override
    public String toString() {
        return "DingDingFeedCardMessage{" +
                "feedCard=" + feedCard +
                '}';
    }

    public static class FeedCard implements Serializable {
        private static final long serialVersionUID = -40076153064340268L;
        /**
         * 2023-08-17 19:22
         * 多条文本
         * 必填
         */
        @Schema(description = "多条文本", required = true)
        private List<Link> links;

        public FeedCard() {
        }

        public FeedCard(List<Link> links) {
            this.links = links;
        }

        public List<Link> getLinks() {
            return links;
        }

        public void setLinks(List<Link> links) {
            this.links = links;
        }

        @Override
        public String toString() {
            return "FeedCard{" +
                    "links=" + links +
                    '}';
        }
    }

    public static class Link implements Serializable {
        private static final long serialVersionUID = 8880452160992860287L;
        /**
         * 2023-08-17 17:48
         * 单条信息文本
         * 必填
         */
        @Schema(description = "单条信息文本", required = true)
        private String title;


        /**
         * 2023-08-17 17:49
         * 点击单条信息到跳转链接。
         * 必填
         */
        @Schema(description = "点击单条信息到跳转链接", required = true)
        private String messageUrl;

        /**
         * 2023-08-17 17:47
         * 单条信息后面图片的URL。
         * 必填
         */
        @Schema(description = "单条信息后面图片的URL", required = true)
        private String picUrl;

        public Link() {
        }

        public Link(String title, String messageUrl, String picUrl) {
            this.title = title;
            this.messageUrl = messageUrl;
            this.picUrl = picUrl;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
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
                    ", messageUrl='" + messageUrl + '\'' +
                    ", picUrl='" + picUrl + '\'' +
                    '}';
        }
    }
}
