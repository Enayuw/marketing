package com.br.marketing.dto.mock;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @ClassName MockPolicyDTO
 * @Author kongbx
 * @Date 2025/7/1 16:26
 */
@Data
@ApiModel("MockDTO")
public class MockPolicyDTO {

    @ApiModelProperty("ids")
    private List<Long> ids;

    @ApiModelProperty("是否启用 0-启动 1-关闭")
    private Integer enabled;

}
