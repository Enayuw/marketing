package com.br.marketing.origin;

import lombok.Data;



@Data
public class MrpMqFact {

    /**
     * apiCode信息
     */
    private String apiCode;

    /**
     *  mq中数据id
     */
    private String sourceId;

    /**
     *  消息来源 数据来源于 TransferSource枚举类
     * @see TransferSource
     */
    private Integer source;

}
