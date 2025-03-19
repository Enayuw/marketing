package com.br.marketing.dto.tag;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;
import java.util.List;

/**
 * 标签查询DTO
 */
@Data
@ApiModel("标签查询DTO")
public class TagQueryDTO {

    @ApiModelProperty("当前页码")
    @Min(value = 1, message = "页码必须大于0")
    private Integer current = 1;

    @ApiModelProperty("每页大小")
    @Min(value = 1, message = "每页大小必须大于0")
    private Integer size = 10;

    @ApiModelProperty("标签名称")
    private String tagName;

    @ApiModelProperty("授权APICode列表")
    private List<String> apiCodes;

    @ApiModelProperty("创建人ID")
    private Long creator;

    @ApiModelProperty("排序字段")
    private String orderByField = "update_time";

    @ApiModelProperty("排序方式")
    private String orderByType = "DESC";

    @ApiModelProperty("当前用户ID")
    private Long currentUserId;

//    @ApiModelProperty("状态")
//    private Boolean status;
}