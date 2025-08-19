package com.br.marketing.common.constants;

public class PulsarTopic {

    /**
     * 持久化（persistent|non-persistent）//租户（tenant）/命名空间（namespace）
     */
    public final static String pulsarPreFix = "persistent://CDC/API/";

    public final static String topicPreFix = "marketing-";

    /**
     * 标准上传接口主题
     */
    public final static String upLoadTopic = pulsarPreFix.concat(topicPreFix).concat("upload-base");

    /**
     * 数禾上传接口主题
     */
    public final static String upLoadShTopic = pulsarPreFix.concat(topicPreFix).concat("upload-sh");

    /**
     * 标准转化接口主题
     */
    public final static String transferTopic = pulsarPreFix.concat(topicPreFix).concat("transfer-base");

    /**
     * 数禾转化接口主题
     */
    public final static String transferShTopic = pulsarPreFix.concat(topicPreFix).concat("transfer-sh");

    /**
     * 定制客户转化接口主题
     */
    public final static String transferCustomTopic = pulsarPreFix.concat(topicPreFix).concat("transfer-custom");
    /**
     * 定制客户上传接口主题
     */
    public final static String uploadCustomTopic = pulsarPreFix.concat(topicPreFix).concat("upload-custom");
}
