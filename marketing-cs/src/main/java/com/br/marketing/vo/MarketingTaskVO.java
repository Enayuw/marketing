package com.br.marketing.vo;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * -------------------------------
 *
 * @author guangchao.zhang
 * @Description
 * @Date 2022/5/10 12:03 PM
 * ------------------------------
 */
@Data
public class MarketingTaskVO {
    /**
     *
     */
    @ApiModelProperty(value = "id")
    private Long id;

    @ApiModelProperty(value = "跑分历史id")
    private Long hisFileId;

    /**
     * 规则名称
     */
    @ApiModelProperty(value = "规则名称")
    private String ruleName;

    /**
     * 规则编号
     */
    @ApiModelProperty(value = "规则编号")
    private String ruleNumber;

    /**
     * 商户编号
     */
    @ApiModelProperty(value = "apiCode")
    private String apiCode;


    @ApiModelProperty(value = "客户编号")
    private String cid;


    @ApiModelProperty(value = "客户名称")
    private String cName;

    /**
     * 状态码1-开启；0-关闭
     */
    @ApiModelProperty(value = "使用状态")
    private Integer status;

    /**
     * 跑分状态:
     * 待开始--没有关联关系
     * status=3，跑分中  进行中
     * status=1，待合并
     * status=0，待传输
     * status=2，已完毕
     */
    @ApiModelProperty(value = "跑分状态")
    private Integer taskStatus;

    /**
     * 跑分日期
     */
    @ApiModelProperty(value = "跑分日期")
    private String taskTime;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间")
    private String createTime;

    /**
     * 修改时间
     */
    @ApiModelProperty(value = "修改时间")
    private String updateTime;

    @ApiModelProperty(value = "周期跑分开始时间")
    private String startDate;

    @ApiModelProperty(value = "周期跑分结束时间")
    private String closeDate;
    @ApiModelProperty(value = "数据量")
    private Integer taskNumber;

    private String conditionInfo;

    /**
     * 排序
     */
    private Integer priority;

    /**
     * 跑分文件名
     */
    private String fileName;

    /**
     * 周期类型 任务执行策略 1-一次性全量；2-周期性全量
     */
    @ApiModelProperty(value = "周期类型")
    private String execType;

    @ApiModelProperty(value = "跑分范围类型 1-当天数据范围；2-手动选择数据范围")
    private String conditionType;
    /**
     * 创建时间
     */
    @ApiModelProperty(value = "跑分时间")
    private String startTime;

    @ApiModelProperty(value = "数据范围展示")
    private String conditionInfoShow;

    private String batchNumber;

    @ApiModelProperty(value = "")
    private Integer isOnline;

    @ApiModelProperty(value = "跑分开始时间")
    private String taskCreateTime;

    @ApiModelProperty(value = "跑分结束时间")
    private String taskUpdateTime;

}
