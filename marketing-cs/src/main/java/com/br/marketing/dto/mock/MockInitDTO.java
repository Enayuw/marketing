package com.br.marketing.dto.mock;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @ClassName MockInitDTO
 * @Author kongbx
 * @Date 2025/6/30 13:49
 */
@Data
@Schema(description = "Mock初始化DTO")
public class MockInitDTO {
    @Schema(description = "mock名称")
    private String mockName;

    @Schema(description = "是否启用 0-启动 1-关闭")
    private Integer enabled;

    @Schema(description = "版本号")
    private String version;

}
