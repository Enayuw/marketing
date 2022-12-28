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
}
