package com.br.marketing.dto.mock;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;
import java.util.Date;

/**
 * @ClassName MockQueryDTO
 * @Author kongbx
 * @Date 2025/6/6 15:59
 */
@Data
@ApiModel("Mock策略查询DTO")
public class MockQueryDTO {
    @ApiModelProperty("当前页码")
    @Min(value = 1, message = "页码必须大于0")
    private Integer current = 1;

    @ApiModelProperty("每页大小")
    @Min(value = 1, message = "每页大小必须大于0")
    private Integer size = 10;

    @ApiModelProperty("Mock名称")
    private String mockName;

    @ApiModelProperty("是否启用 0-启动 1-关闭")
    private Integer enabled;

    @ApiModelProperty("更新时间")
    private Date updateTime;

}
