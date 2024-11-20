package com.br.marketing.origin;

import lombok.Data;



@Data
public class MrpMqFact {

    /**
     *  mq中数据id
     */
    private Long sourceId;

    /**
     *  消息来源 数据来源于 TransferSource枚举类
     * @see TransferSource
     */
    private Integer source;

}
