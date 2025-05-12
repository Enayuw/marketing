package com.br.marketing.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;

/**
 * @ClassName CarClueChannelConfigDTO
 * @Author kongbx
 * @Date 2025/5/6 13:45
 */
@Data
public class CarClueChannelConfigDTO {
    @ApiModelProperty(value = "id")
    private Long id;
    @ApiModelProperty(value = "易车KA拉取时间")
    private String pullDate;
    @ApiModelProperty(value = "外呼意向等级配置")
    private String intentionConfig;
    @ApiModelProperty(value = "数据清洗类型 0-手动执行 1-自动执行")
    private int cleanType;
    @ApiModelProperty(value = "数据推送类型 0-手动执行 1-自动执行")
    private int pullType;
    @NotNull(message = "操作人id不能为空")
    @ApiModelProperty(value = "操作人id", required = true)
    private Long optUserId;
    @NotEmpty(message = "操作人账户名不能为空")
    @ApiModelProperty(value = "操作人账户名", required = true)
    private String optUserName;
}
