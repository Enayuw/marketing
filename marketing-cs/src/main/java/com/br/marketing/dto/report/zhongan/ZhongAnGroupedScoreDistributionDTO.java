package com.br.marketing.dto.report.zhongan;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import lombok.Data;

/**
 * 众安分组评分分布dto
 *
 * @author senyang.zheng
 * @date 2024/09/20
 */
@Data
@Schema(description = "众安分组评分分布")
public class ZhongAnGroupedScoreDistributionDTO {
    @Schema(description = "跑分产品")
    private String product;
    @Schema(description = "区间")
    private String interval;
    @Schema(description = "分组")
    private String group;
    @Schema(description = "名称")
    private String name;
    @Schema(description = "量级")
    private Long num;
    @Schema(description = "占比")
    private BigDecimal proportion;
    @Schema(description = "步长")
    private Integer step;

}
