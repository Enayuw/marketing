package com.br.marketing.dto.mock;

import lombok.Data;
import org.apache.pulsar.shade.io.swagger.annotations.ApiModel;
import org.apache.pulsar.shade.io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

/**
 * @ClassName MockCreateCaseDTO
 * @Description Mock新增修改用例
 * @Author kongbx
 * @Date 2025/7/1 16:26
 */
@Data
@ApiModel("Mock新增修改用例DTO")
public class MockCreateCaseDTO implements Serializable {

    @ApiModelProperty("id")
    private Long id;

    @ApiModelProperty("Mock名称")
    private String mockName;

    @ApiModelProperty("Mock用例名称")
    private String mockCaseName;

    @ApiModelProperty("apiCode")
    private String apiCode;

    @ApiModelProperty("返回响应")
    private String responseBody;

    @ApiModelProperty("响应状态码")
    private Integer statusCode;

    @ApiModelProperty("延迟毫秒数")
    private Integer delayMs;

    @ApiModelProperty("延迟波动（百分比）")
    private Integer delayFluctuation;

    @ApiModelProperty("描述")
    private String description;

    @ApiModelProperty("是否启用 0-启动 1-关闭")
    private Integer enabled;
}
