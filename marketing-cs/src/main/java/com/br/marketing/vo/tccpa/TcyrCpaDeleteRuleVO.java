package com.br.marketing.vo.tccpa;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TcyrCpaDeleteRuleVO {

    /**
     *
     */
    private Long id;

    /**
     * 客户编号
     */
    private String cid;

    /**
     * 客户名称
     */
    private String customerName;

    /**
     * 商户编号
     */
    private String apiCode;

    /**
     * 剔除规则名称
     */
    private String ruleName;

    /**
     * 剔除规则类型 1-周期锁定；2-大空白组；3-failMsg；4-自定义
     */
    private Integer ruleType;

    /**
     * 失败类型 1-黑名单；3-已转化；4-无此用户；5-达到限额
     */
    private String failMsgs;

    /**
     * 执行脚本
     */
    private String executeScript;

    /**
     * 规则量级
     */
    private Integer deleteNum;

    /**
     * 禁用标志 0-禁用 1-启用
     */
    private Integer enabled;

    /**
     * 删除状态 1-可用 9-删除
     */
    private Integer isDel;

    /**
     * 扩展字段
     */
    private String extend;

    /**
     *
     */
    private Date createTime;

    /**
     *
     */
    private Date updateTime;
}
