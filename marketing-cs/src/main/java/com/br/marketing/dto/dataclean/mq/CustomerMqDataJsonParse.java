package com.br.marketing.dto.dataclean.mq;

import lombok.Data;
/**
 * @desc :客户原始数据解析json消息实例
 * @author
 */
@Data
public class CustomerMqDataJsonParse extends MqDataJsonParse {

    /**
     * 数据主键Id
     */
    private Long dataId;



}
