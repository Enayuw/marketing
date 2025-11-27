package com.br.marketing.dto.mock;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @ClassName MockCreateCaseDTO
 * @Description Mock新增修改用例
 * @Author kongbx
 * @Date 2025/7/1 16:26
 */
@Data
@Schema(description = "Mock新增修改用例DTO")
public class MockCreateCaseDTO implements Serializable {

    @Schema(description = "id")
    private Long id;

    @Schema(description = "Mock名称")
    private String mockName;

    @Schema(description = "Mock用例名称")
    private String mockCaseName;

    @Schema(description = "apiCode")
    private String apiCode;

    @Schema(description = "返回响应")
    private String responseBody;

    @Schema(description = "响应状态码")
    private Integer statusCode;

    @Schema(description = "延迟毫秒数")
    private Integer delayMs;

    @Schema(description = "延迟波动（百分比）")
    private Integer delayFluctuation;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "是否启用 0-启动 1-关闭")
    private Integer enabled;
}
