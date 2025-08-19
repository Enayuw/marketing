package com.br.marketing.common.constants;

public class PulsarSubscription {
    
    public final static String subscriptionPreFix = "marketing_";

    /**
     * 标准上传接口订阅者
     * 主题 {@link com.br.marketing.common.constants.PulsarTopic#upLoadTopic}
     */
    public final static String upLoadSubscription = subscriptionPreFix.concat("upload_base");

    /**
     * 数禾上传接口订阅者
     * 主题 {@link com.br.marketing.common.constants.PulsarTopic#upLoadShTopic}
     */
    public final static String upLoadShSubscription = subscriptionPreFix.concat("upload_sh");

    /**
     * 标准转化接口订阅者
     * 主题 {@link com.br.marketing.common.constants.PulsarTopic#transferTopic}
     */
    public final static String transferSubscription = subscriptionPreFix.concat("transfer_base");

    /**
     * 数禾转化接口订阅者
     * 主题 {@link com.br.marketing.common.constants.PulsarTopic#transferShTopic}
     */
    public final static String transferShSubscription = subscriptionPreFix.concat("transfer_sh");

    /**
     * 定制客户转化接口订阅者
     * 主题 {@link com.br.marketing.common.constants.PulsarTopic#transferCustomTopic}
     */
    public final static String transferCustomSubscription = subscriptionPreFix.concat("transfer_custom");

    /**
     * 定制客户上传接口订阅者
     * 主题 {@link com.br.marketing.common.constants.PulsarTopic#uploadCustomTopic}
     */
    public final static String uploadCustomSubscription = subscriptionPreFix.concat("upload_custom");
}
