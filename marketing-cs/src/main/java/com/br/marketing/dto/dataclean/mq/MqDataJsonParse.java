package com.br.marketing.dto.dataclean.mq;

import com.br.marketing.origin.BaseMqFact;
import lombok.Data;
/**
 * @desc :客户原始数据解析json消息实例
 * @author
 */
@Data
public class MqDataJsonParse extends BaseMqFact {

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

    /**
     * 数据主键Id
     */
    private Long dataId;



}
