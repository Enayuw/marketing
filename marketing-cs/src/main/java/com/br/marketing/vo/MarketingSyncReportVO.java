package com.br.marketing.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


@Data
public class MarketingSyncReportVO {
    /**
     * 
     */
    @ApiModelProperty(value = "主键id")
    private Long id;

    /**
     * 客户编号
     */
    @ApiModelProperty(value = "客户编号")
    private String cid;

    /**
     * 商户编号
     */
    @ApiModelProperty(value = "商户编号")
    private String apiCode;

    /**
     * 客户名称
     */
    @ApiModelProperty(value = "客户名称")
    private String shortName;

    /**
     * 上传日期
     */
    @ApiModelProperty(value = "上传日期")
    private String appletDate;

    /**
     * 场景
     */
    @ApiModelProperty(value = "场景")
    private String userType;

    /**
     * 数据正常入库条数
     */
    @ApiModelProperty(value = "数据正常入库条数")
    private Integer normalNum;

    /**
     * 去重后数据量
     */
    @ApiModelProperty(value = "去重后数据量")
    private Integer duplicateRemovalNum;

    /**
     * 上传开始时间
     */
    @ApiModelProperty(value = "上传开始时间")
    private String appletBeginTime;

    /**
     * 上传结束时间
     */
    @ApiModelProperty(value = "上传结束时间")
    private String appletEndTime;

    /**
     * 数据生效日期
     */
    @ApiModelProperty(value = "数据生效日期")
    private String validStartDate;

    /**
     * 数据失效日期
     */
    @ApiModelProperty(value = "数据失效日期")
    private String validEndDate;

    /**
     * 备注
     */
    @ApiModelProperty(value = "备注")
    private String remark;

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

}