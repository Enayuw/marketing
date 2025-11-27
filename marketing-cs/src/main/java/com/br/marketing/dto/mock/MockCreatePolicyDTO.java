package com.br.marketing.dto.mock;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @ClassName MockCreatePolicyDTO
 * @Description Mock新增修改策略
 * @Author kongbx
 * @Date 2025/7/1 16:26
 */
@Data
@Schema(description = "Mock新增修改策略DTO")
public class MockCreatePolicyDTO implements Serializable {

    @Schema(description = "id")
    private Long id;

    @Schema(description = "Mock名称")
    private String mockName;

    @Schema(description = "策略类型")
    private Integer mockPolicyType;

    @Schema(description = "是否启用 0-启动 1-关闭")
    private Integer enabled;

    @Schema(description = "版本号")
    private String version;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "mock用例")
    private List<MockCreateCaseDTO> mockCreateCaseDTOS;

}
