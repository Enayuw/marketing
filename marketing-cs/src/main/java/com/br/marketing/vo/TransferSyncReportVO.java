package com.br.marketing.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;


@Data
public class TransferSyncReportVO {
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
     * 数据入库条数
     */
    @Schema(description = "数据入库条数")
    private String dataCount;

    /**
     * 上传开始时间
     */
    @Schema(description = "上传开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date appletBeginTime;

    /**
     * 上传结束时间
     */
    @Schema(description = "上传结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date appletEndTime;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 修改时间
     */
    @Schema(description = "修改时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

}