/**
  * Copyright 2021 bejson.com 
  */
package com.br.marketing.client.robotaiapi.input;
import com.br.marketing.rule.InterfaceParams;
import com.br.marketing.rule.SourceData;
import lombok.*;

/**
 * Auto-generated: 2021-08-04 10:58:58
 */
@Getter
@Setter
@ToString
public class ConversionData extends SourceData {

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
    private String cid;

    /**
     * 数据id
     */
    private String dataId;

    /**
     * 转化数据类型 1:实时 2:非实时
     */
    private String transformType;

    /**
     * 生效开始时间 格式yyyy-mm-dd HH:mm:ss
     */
    private String effectiveDate;

    /**
     * 生效截止时间 格式yyyy-mm-dd HH:mm:ss
     */
    private String expireDate;
}