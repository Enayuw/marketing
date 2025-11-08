package com.br.marketing.dto.mock;

import lombok.Data;
import org.apache.pulsar.shade.io.swagger.annotations.ApiModel;
import org.apache.pulsar.shade.io.swagger.annotations.ApiModelProperty;

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
