package com.br.marketing.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class MarketingCustomerVO {

    /**
     * 主键id
     */
    @ApiModelProperty(value = "主键id")
    private String id;

    /**
     * api_code
     */
    @ApiModelProperty(value = "api_code")
    private String apiCode;

    /**
     * 合作客户ID
     */
    @ApiModelProperty(value = "合作客户ID")
    private String cid;

    /**
     * 合作客户名称
     */
    @ApiModelProperty(value = "合作客户名称")
    private String name;

    /**
     * 合作客户简称
     */
    @ApiModelProperty(value = "合作客户简称")
    private String shortName;


}
