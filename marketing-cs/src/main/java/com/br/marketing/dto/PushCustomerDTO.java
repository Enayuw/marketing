package com.br.marketing.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

@Data
public class PushCustomerDTO {

    @ApiModelProperty(value = "商户编号")
    @NotNull(message = "商户编个号不能为空")
    private String apiCode;

    @ApiModelProperty(value = "上传开始时间")
    @NotNull(message = "上传开始时间不能为空")
    private String uploadBeginTime;

    @ApiModelProperty(value = "上传结束时间")
    @NotNull(message = "上传结束时间不能为空")
    private String uploadEndTime;

    @ApiModelProperty(value = "跑分执行开始时间")
    @NotNull(message = "跑分执行开始时间不能为空")
    private String scoreBeginTime;

    @ApiModelProperty(value = "跑分执行结束时间")
    @NotNull(message = "跑分执行结束时间不能为空")
    private String scoreEndTime;

    @ApiModelProperty(value = "批次号")
    @NotNull(message = "批次号不能为空")
    @NotEmpty(message = "批次号不能为空")
    @Size(min = 1,message = "批次号不能为空")
    private List<String> batchNumberList;

    @ApiModelProperty(value = "跑分记录id")
    @NotNull(message = "fileIdList不能为空")
    @NotEmpty(message = "fileIdList不能为空")
    @Size(min = 1,message = "fileIdList不能为空")
    private List<Long> fileIdList;

    @ApiModelProperty(value = "查询规则")
    private String mRuleCondition;

    @ApiModelProperty(value = "查询规则用于前端展示文本")
    private String mRuleConditionShow;

    @ApiModelProperty(value = "推送数量")
    private Integer mPlanNum;

    @ApiModelProperty(value = "百分比")
    private BigDecimal mPercentage;


}
