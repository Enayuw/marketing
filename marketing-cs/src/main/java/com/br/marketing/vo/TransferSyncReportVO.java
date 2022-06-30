package com.br.marketing.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;


@Data
public class TransferSyncReportVO {
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
     * 数据入库条数
     */
    @ApiModelProperty(value = "数据入库条数")
    private String dataCount;

    /**
     * 上传开始时间
     */
    @ApiModelProperty(value = "上传开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date appletBeginTime;

    /**
     * 上传结束时间
     */
    @ApiModelProperty(value = "上传结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date appletEndTime;

    /**
     * 备注
     */
    @ApiModelProperty(value = "备注")
    private String remark;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 修改时间
     */
    @ApiModelProperty(value = "修改时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

}