/**
  * Copyright 2021 bejson.com 
  */
package com.br.marketing.client.robotaiapi.input;
import lombok.Data;

/**
 * Auto-generated: 2021-08-04 10:58:58
 */
@Data
public class ConversionData {

    /**
     * 案件编号
     */
    private String caseNum;

    /**
     *场景类型
     */
    private String groupType;

    /**
     * 转化日期
     */
    private String inversionDate;

    /**
     * 转化状态
     */
    private String inversionStatus;

    /**
     * 原始转化信息
     */
    private String inversionInfo;

    /**
     * 拓展字段
     */
    private String jsonInfo;

    /**
     * 手机号
     */
    private String phone;
    /**
     * 任务id 任务标识
     */
    private String taskId;
    /**
     * 合作平台入库时间
     */
    private String partnerProcessDate;
    /**
     * 1:数禾,2:萨摩耶 (必填)
     */
    private String businessType;
}