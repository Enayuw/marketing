package com.br.marketing.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Data
public class MarketingSyncReportVO {
    /**
     * 
     */
    @Schema(description = "主键id")
    private Long id;

    /**
     * 客户编号
     */
    @Schema(description = "客户编号")
    private String cid;

    /**
     * 商户编号
     */
    @Schema(description = "商户编号")
    private String apiCode;

    /**
     * 客户名称
     */
    @Schema(description = "客户名称")
    private String shortName;

    /**
     * 上传日期
     */
    @Schema(description = "上传日期")
    private String appletDate;

    /**
     * 场景
     */
    @Schema(description = "场景")
    private String userType;

    /**
     * 数据正常入库条数
     */
    @Schema(description = "数据正常入库条数")
    private Integer normalNum;

    /**
     * 去重后数据量
     */
    @Schema(description = "去重后数据量")
    private Integer duplicateRemovalNum;

    /**
     * 上传开始时间
     */
    @Schema(description = "上传开始时间")
    private String appletBeginTime;

    /**
     * 上传结束时间
     */
    @Schema(description = "上传结束时间")
    private String appletEndTime;

    /**
     * 数据生效日期
     */
    @Schema(description = "数据生效日期")
    private String validStartDate;

    /**
     * 数据失效日期
     */
    @Schema(description = "数据失效日期")
    private String validEndDate;

    /**
     * 标签量级
     */
    @Schema(description = "标签量级")
    private String labelMessage;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;

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

}