//package com.br.marketing.entity.rocketmq;
//
//import java.util.Date;
//
//public class MarketingMqMsgSole {
//    /**
//     *
//     */
//    private Long id;
//
//    /**
//     *
//     */
//    private String topic;
//
//    /**
//     *
//     */
//    private String tags;
//
//    /**
//     * mq消息内容
//     */
//    private String message;
//
//    /**
//     * RocketMQ自带的消息唯一标识
//     */
//    private String msgId;
//
//    /**
//     * 生产者给每条消息的唯一标识
//     */
//    private String msgProductId;
//
//    /**
//     * 创建时间
//     */
//    private Date createTime;
//
//    /**
//     *
//     */
//    private Date updateTime;
//
//    /**
//     * 1-有效；9-无效
//     */
//    private Integer isDel;
//
//    public Long getId() {
//        return id;
//    }
//
//    public void setId(Long id) {
//        this.id = id;
//    }
//
//    public String getTopic() {
//        return topic;
//    }
//
//    public void setTopic(String topic) {
//        this.topic = topic == null ? null : topic.trim();
//    }
//
//    public String getTags() {
//        return tags;
//    }
//
//    public void setTags(String tags) {
//        this.tags = tags == null ? null : tags.trim();
//    }
//
//    public String getMessage() {
//        return message;
//    }
//
//    public void setMessage(String message) {
//        this.message = message == null ? null : message.trim();
//    }
//
//    public String getMsgId() {
//        return msgId;
//    }
//
//    public void setMsgId(String msgId) {
//        this.msgId = msgId == null ? null : msgId.trim();
//    }
//
//    public String getMsgProductId() {
//        return msgProductId;
//    }
//
//    public void setMsgProductId(String msgProductId) {
//        this.msgProductId = msgProductId == null ? null : msgProductId.trim();
//    }
//
//    public Date getCreateTime() {
//        return createTime;
//    }
//
//    public void setCreateTime(Date createTime) {
//        this.createTime = createTime;
//    }
//
//    public Date getUpdateTime() {
//        return updateTime;
//    }
//
//    public void setUpdateTime(Date updateTime) {
//        this.updateTime = updateTime;
//    }
//
//    public Integer getIsDel() {
//        return isDel;
//    }
//
//    public void setIsDel(Integer isDel) {
//        this.isDel = isDel;
//    }
//}