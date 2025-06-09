package com.br.marketing.vo;

import io.swagger.annotations.ApiModelProperty;
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
    @ApiModelProperty(value = "id")
    private Long id;
    @ApiModelProperty(value = "客户编号")
    private String apiCode;
    @ApiModelProperty(value = "品牌id")
    private int brandId;
    @ApiModelProperty(value = "品牌名称")
    private String brandName;
    @ApiModelProperty(value = "车系id")
    private int seriesId;
    @ApiModelProperty(value = "车系名称")
    private String seriesName;
    @ApiModelProperty(value = "固定省名称")
    private String satisfyProvinceName;
    @ApiModelProperty(value = "固定市名称")
    private String satisfyCityName;
    @ApiModelProperty(value = "排除省名称")
    private String excludeProvinceName;
    @ApiModelProperty(value = "排除市名称")
    private String excludeCityName;
    @ApiModelProperty(value = "省市类型 0-全国 1-固定 2-排除")
    private int provinceType;
    @ApiModelProperty(value = "会员ID")
    private String demandId;
    @ApiModelProperty(value = "需求量")
    private int dailyLimited;
    @ApiModelProperty(value = "创建时间")
    private String createTime;
}
