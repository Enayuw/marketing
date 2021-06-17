package com.br.marketing.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Data
public class PushCustomerDTO {

    @ApiModelProperty(value = "商户编号")
    @NotNull(message = "商户编个号不能为空")
    private String apiCode;

    @ApiModelProperty(value = "产品名称")
    @NotNull(message = "产品名称不能为空")
    private String productName;

    @ApiModelProperty(value = "产品版本")
    @NotNull(message = "产品版本不能为空")
    private String productVersion;

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
    private List<Integer> fileIdList;

    @ApiModelProperty(value = "最小分数")
    private Integer minScore;

    @ApiModelProperty(value = "最大分数")
    private Integer maxScore;

    @ApiModelProperty(value = "最小top值")
    private Integer minTop;

    @ApiModelProperty(value = "最大top值")
    private Integer maxTop;


}
