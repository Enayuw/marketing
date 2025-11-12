package com.br.marketing.dto.tag;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Min;
import java.util.List;

/**
 * 标签查询DTO
 */
@Data
@Schema(description = "标签查询DTO")
public class TagQueryDTO {

    @Schema(description = "当前页码")
    @Min(value = 1, message = "页码必须大于0")
    private Integer current = 1;

    @Schema(description = "每页大小")
    @Min(value = 1, message = "每页大小必须大于0")
    private Integer size = 10;

    @Schema(description = "标签名称")
    private String tagName;

    @Schema(description = "授权APICode列表")
    private List<String> apiCodes;

    @Schema(description = "创建人ID")
    private Long creator;

    @Schema(description = "排序字段")
    private String orderByField = "update_time";

    @Schema(description = "排序方式")
    private String orderByType = "DESC";

    @Schema(description = "当前用户ID")
    private Long currentUserId;

//    @Schema(description = "状态")
//    private Integer status;
}