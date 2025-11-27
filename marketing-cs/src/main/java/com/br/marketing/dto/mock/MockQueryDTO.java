package com.br.marketing.dto.mock;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Min;
import java.util.Date;

/**
 * @ClassName MockQueryDTO
 * @Author kongbx
 * @Date 2025/6/6 15:59
 */
@Data
@Schema(description = "Mock策略查询DTO")
public class MockQueryDTO {
    @Schema(description = "当前页码")
    @Min(value = 1, message = "页码必须大于0")
    private Integer current = 1;

    @Schema(description = "每页大小")
    @Min(value = 1, message = "每页大小必须大于0")
    private Integer size = 10;

    @Schema(description = "Mock名称")
    private String mockName;

    @Schema(description = "是否启用 0-启动 1-关闭")
    private Integer enabled;

    @Schema(description = "更新时间")
    private Date updateTime;

}
