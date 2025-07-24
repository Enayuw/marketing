package com.br.marketing.client.intelligentcustomerservice.input;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.rule.SourceData;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 用于规则流转进行组装的对象
 */
@Getter
@Setter
@ToString
public class PushMarketingUserDetailByRuleDTO extends SourceData {

    /**
     * 数据集id
     */
    private String batchNumber;

    /**
     * 案件编号
     */
    private String caseNumber;

    /**
     *手机号码
     */
    private String phone;

    /**
     *手机号码（log加密）
     */
    private String logCell;

    /**
     *变量JSON
     */
    private JSONObject variables;

    /**
     * 触达策略
     */
    private String strategyCode;

    /**
     * 场景status
     */
    private String status;

    /**
     *手机号码 明文
     * 用于存入b_data_distribute_detail_log表cell统一格式
     */
    private String cell;
    /**
     * 要推送的apiCode
     */
    private String pushApiCode;
    /**
     * 数据集合名称
     */
    private String batchName;
}
