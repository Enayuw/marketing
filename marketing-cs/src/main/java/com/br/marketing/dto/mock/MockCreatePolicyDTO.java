package com.br.marketing.dto.mock;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @ClassName MockCreatePolicyDTO
 * @Description Mock新增修改策略
 * @Author kongbx
 * @Date 2025/7/1 16:26
 */
@Data
@ApiModel("Mock新增修改策略DTO")
public class MockCreatePolicyDTO {

    @ApiModelProperty("id")
    private Long id;

    @ApiModelProperty("Mock名称")
    private String mockName;

    @ApiModelProperty("策略类型")
    private Integer mockPolicyType;

    @ApiModelProperty("是否启用 0-启动 1-关闭")
    private Integer enabled;

    @ApiModelProperty("版本号")
    private String version;

    @ApiModelProperty("描述")
    private String description;

}
