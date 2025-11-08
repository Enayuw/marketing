package com.br.marketing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class CustomerBatchNumDTO {

    @Schema(description = "商户编号")
    @NotNull(message = "商户编个号不能为空")
    private String apiCode;

    @Schema(description = "产品名称")
    private String productName;

    @Schema(description = "产品版本")
    private String productVersion;

    @Schema(description = "上传开始时间")
    private String uploadBeginTime;

    @Schema(description = "上传结束时间")
    private String uploadEndTime;

    @Schema(description = "跑分时间区间，多段")
    private List<ScoreTimeDTO> scoreTimeList;

    @Schema(description = "场景")
    private String userType;

    private Integer current;

    private Integer size;

    private List<String> moduleList;


}
