package com.br.marketing.dto.dataclean.mq;

import lombok.Data;

/**
 * @ClassName MqCommonDataJsonParse
 * @Author hang.zhou
 * @Date 2025/11/11
 */
@Data
public class MqDataJsonParse {

    /**
     * 数据来源,0-营销中台 1-外呼系统
     */
    private Integer systemType;

    /**
     * 数据类型：0上传，1转化
     */
    private Integer dataType;

    /**
     * 接收类型：0:通用,1:定制,2:FTP
     */
    private Integer acceptType;

}
