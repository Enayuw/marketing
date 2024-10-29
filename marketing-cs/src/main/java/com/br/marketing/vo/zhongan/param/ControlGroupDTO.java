package com.br.marketing.vo.zhongan.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;


/**
 * @ClassName ZhongAnControlGroupVO
 * @Description 众安对照组配置
 * @Author kongbx
 * @Date 2024/9/18 15:30
 */
@Data
public class ControlGroupDTO {

    @ApiModelProperty(value = "数据日期")
    private String reportDate;

    @ApiModelProperty(value = "当前页码")
    @NotNull(message = "页码不能为空")
    private Integer current;

    @ApiModelProperty(value = "页容量")
    private Integer size;

}
