package com.br.marketing.dto.tag;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * 更新标签状态DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateTagStatusDTO {
    @ApiModelProperty(value = "标签编码", required = true)
    private String tagCode;

    @ApiModelProperty(value = "状态（1-启用, 0-禁用）", required = true)
    private Integer status;

    @ApiModelProperty(value = "操作人ID", required = true)
    private Long optUserId;
}