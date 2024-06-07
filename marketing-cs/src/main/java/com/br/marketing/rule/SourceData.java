package com.br.marketing.rule;

import com.alibaba.fastjson.annotation.JSONField;
import com.br.marketing.common.enums.DistributeSourceTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Description :去重参数构建
 * ---------------------------------
 * @Author : zhen.Li
 * @Date : Create in 2023/3/21 14:11
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SourceData extends InterfaceParams{
    /**
     * 原始数据id
     */
    @JSONField(serialize = false)
    private Long initId;


    /**
     * 有效期开始时间
     * 格式：yyyy-MM-dd
     */
    @JSONField(serialize = false)
    private String expireBeginDate;

    /**
     * 有效期结束时间
     * 格式：yyyy-MM-dd
     */
    @JSONField(serialize = false)
    private String expireEndDate;


    /**
     * 去重类型：
     * value = -1,表示单条数据计算有效期去重，expireBeginDate，expireEndDate 必传
     * value = [0,+∞]，表示一批数据范围内去重,n表示n天内推送一次，
     */
    @JSONField(serialize = false)
    private Integer soleType;

    /**
     * 去重维度：
     * 见枚举：com.br.marketing.common.enums.SoleFieldEnum
     */
    @JSONField(serialize = false)
    private Integer soleField;

    /**
     * 2023-09-07 10:28
     * 数据来源,默认转化数据 TRANSFER
     */
    @JSONField(serialize = false)
    private DistributeSourceTypeEnum distributeSourceTypeEnum;

}
