package com.br.marketing.vo;

import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(description = "id")
    private Long id;

    @Schema(description = "跑分历史id")
    private Long hisFileId;

    /**
     * 规则名称
     */
    @Schema(description = "规则名称")
    private String ruleName;

    /**
     * 规则编号
     */
    @Schema(description = "规则编号")
    private String ruleNumber;

    /**
     * 商户编号
     */
    @Schema(description = "apiCode")
    private String apiCode;


    @Schema(description = "客户编号")
    private String cid;


    @Schema(description = "客户名称")
    private String cName;

    /**
     * 状态码1-开启；0-关闭
     */
    @Schema(description = "使用状态")
    private Integer status;

    /**
     * 跑分状态:
     * 待开始--没有关联关系
     * status=3，跑分中  进行中
     * status=1，待合并
     * status=0，待传输
     * status=2，已完毕
     */
    @Schema(description = "跑分状态")
    private Integer taskStatus;

    /**
     * 跑分日期
     */
    @Schema(description = "跑分日期")
    private String taskTime;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private String createTime;

    /**
     * 修改时间
     */
    @Schema(description = "修改时间")
    private String updateTime;

    @Schema(description = "周期跑分开始时间")
    private String startDate;

    @Schema(description = "周期跑分结束时间")
    private String closeDate;
    @Schema(description = "数据量")
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
    @Schema(description = "周期类型")
    private String execType;

    @Schema(description = "跑分范围类型 1-当天数据范围；2-手动选择数据范围")
    private String conditionType;
    /**
     * 创建时间
     */
    @Schema(description = "跑分时间")
    private String startTime;

    @Schema(description = "数据范围展示")
    private String conditionInfoShow;

    private String batchNumber;

    @Schema(description = "")
    private Integer isOnline;

    @Schema(description = "跑分开始时间")
    private String taskCreateTime;

    @Schema(description = "跑分结束时间")
    private String taskUpdateTime;

    @Schema(description = "数据条件")
    private String dataCondition;

}
