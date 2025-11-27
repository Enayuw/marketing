package com.br.marketing.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName CarClueChannelVo
 * @Author kongbx
 * @Date 2025/5/6 11:39
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CarClueChannelVo {
    @Schema(description = "id")
    private Long id;
    @Schema(description = "客户编号")
    private String apiCode;
    @Schema(description = "品牌id")
    private int brandId;
    @Schema(description = "品牌名称")
    private String brandName;
    @Schema(description = "车系id")
    private int seriesId;
    @Schema(description = "车系名称")
    private String seriesName;
    @Schema(description = "固定省名称")
    private String satisfyProvinceName;
    @Schema(description = "固定市名称")
    private String satisfyCityName;
    @Schema(description = "排除省名称")
    private String excludeProvinceName;
    @Schema(description = "排除市名称")
    private String excludeCityName;
    @Schema(description = "省市类型 0-全国 1-固定 2-排除")
    private int provinceType;
    @Schema(description = "会员ID")
    private String demandId;
    @Schema(description = "需求量")
    private int dailyLimited;
    @Schema(description = "创建时间")
    private String createTime;
}
