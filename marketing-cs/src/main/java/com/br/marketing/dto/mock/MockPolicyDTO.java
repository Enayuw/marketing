package com.br.marketing.dto.mock;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @ClassName MockPolicyDTO
 * @Author kongbx
 * @Date 2025/7/1 16:26
 */
@Data
@Schema(description = "MockDTO")
public class MockPolicyDTO {

    @Schema(description = "ids")
    private List<Long> ids;

    @Schema(description = "是否启用 0-启动 1-关闭")
    private Integer enabled;

}
