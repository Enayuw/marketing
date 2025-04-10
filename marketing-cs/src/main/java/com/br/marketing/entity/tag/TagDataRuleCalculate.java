package com.br.marketing.entity.tag;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * t_tag_data_rule_calculate
 * @author 
 */
@Data
public class TagDataRuleCalculate implements Serializable {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 标签编码
     */
    private String tagCode;

    /**
     * 计算日期
     */
    private String calculateDate;

    /**
     * 计算量级
     */
    private Integer tagNumber;

    /**
     * 状态：0-待执行；1-执行中；2-执行结束
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    private static final long serialVersionUID = 1L;
}