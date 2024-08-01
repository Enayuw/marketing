package com.br.marketing.client.zhongan.input;

import lombok.Data;

@Data
public class ZaMarketDetail {

    /**
     * 渠道码
     */
    private String  channelCode;

    /**
     * 手机号md5
     */
    private String mobileMd5;

    /**
     * 批次号
     */
    private String taskId;

    /**
     * 营销日期 格式yyyy-MM-dd
     */
    private String bizDate;

    /**
     * MG-营销组；CG-对照组
     */
    private String tag;

    /**
     * 回传时间 实际推送客户时间 格式yyyy-MM-dd HH:mm:ss
     */
    private String postbackDate;

    /**
     * 是否外呼
     */
    private Integer isOutbound;

    /**
     * 是否接通 0-否；1-是
     */
    private Integer isConnect;

    /**
     * 短信是否发送 0-否；1-是
     */
    private Integer isSmsSend;

    /**
     * 短信是否发生成功 0-否；1-是
     */
    private Integer isSmsSendSuccess;


}
