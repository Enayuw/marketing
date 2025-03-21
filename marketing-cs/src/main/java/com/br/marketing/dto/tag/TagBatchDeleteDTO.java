package com.br.marketing.dto.tag;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@ApiModel("标签批量删除DTO")
public class TagBatchDeleteDTO {
    @NotEmpty(message = "标签编码列表不能为空")
    @ApiModelProperty(value = "标签编码列表", required = true)
    private List<String> tagCodes;

    @NotNull(message = "操作人账户名不能为空")
    @ApiModelProperty(value = "当前用户ID", required = true)
    private Long currentUserId;
} 