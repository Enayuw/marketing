package com.br.marketing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class MarketingPreUserDetailDTO implements Serializable {
    private static final long serialVersionUID = 1;

    @Schema(description = "手机号")
    @NotNull(message = "cell必传")
    @NotEmpty(message = "cell必传")
    private String cell;

    private String cellMd5;

    private String cellSha256;

    @Schema(description = "身份证号")
    private String id;

    @Schema(description = "姓名")
    private String name;

    @Schema(description = "手机号原文")
    private String cellOriginal;

    @Schema(description = "身份证号原文")
    private String idOriginal;

    @Schema(description = "姓名原文")
    private String nameOriginal;


    @Schema(description = "场景：促首登、促申完、促动之")
    private String groupType;

    @Schema(description = "用户唯一编号，回调时用到")
    @NotNull(message = "custNum必传")
    @NotEmpty(message = "custNum必传")
    private String custNum;

    @Schema(description = "操作类型")
    private String operateType;

    @Schema(description = "")
    private String registerDate;

    @Schema(description = "业务保留字段1")
    private String reserveField1;

    @Schema(description = "业务保留字段2")
    private String reserveField2;

    @Schema(description = "执行日期")
    private String appletDate;

    @Schema(description = "类型 MD5、Sha256")
    private String failType;

    @Schema(description = "预留剔除状态字段 1：正常，2：剔除")
    private Integer status;

    private String cusBatch;

    /**
     * 任务id
     */
    private String taskId;

    /**
     * 请求id
     */
    private String requestId;

    /**
     * 2025/7/7 13:46
     * 数据指纹，数据唯一标识
     */
    private Long fingerprint;

}
