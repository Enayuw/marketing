package com.br.marketing.dto.report.zhongan;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.math.BigDecimal;
import lombok.Data;

/**
 * 众安分组评分分布dto
 *
 * @author senyang.zheng
 * @date 2024/09/20
 */
@Data
@ApiModel(value = "众安分组评分分布")
public class ZhongAnGroupedScoreDistributionDTO {
    @ApiModelProperty("跑分产品")
    private String product;
    @ApiModelProperty("区间")
    private String interval;
    @ApiModelProperty("分组")
    private String group;
    @ApiModelProperty("名称")
    private String name;
    @ApiModelProperty("任务名称")
    private String reportTaskName;
    @ApiModelProperty("量级")
    private Long num;
    @ApiModelProperty("占比")
    private BigDecimal proportion;
    @ApiModelProperty("步长")
    private Integer step;

}
