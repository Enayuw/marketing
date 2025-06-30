package com.br.marketing.dto.mock;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @ClassName MockInitDTO
 * @Author kongbx
 * @Date 2025/6/30 13:49
 */
@Data
@ApiModel("Mock初始化DTO")
public class MockInitDTO {
    @ApiModelProperty("mock名称")
    private String mockName;

    @ApiModelProperty("是否启用 0-启动 1-关闭")
    private Integer enabled;

    @ApiModelProperty("版本号")
    private String version;

}
