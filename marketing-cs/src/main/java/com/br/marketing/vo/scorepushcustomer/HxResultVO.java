package com.br.marketing.vo.scorepushcustomer;

import lombok.Data;

@Data
public class HxResultVO {
    /**
     *  获取跑批es的字段名
     *  外层基本字段-apiCode,cusNum,idCard,cell,name,swiftNumber,requestTime,batchNumber,cusBatchNumber,taskId,userType,scoreTime
     */
    private String sourceKey;

    /**
     *  映射客户接口的字段名
     */
    private String mappingKey;

    /**
     * 值
     */
    private String value;
}
