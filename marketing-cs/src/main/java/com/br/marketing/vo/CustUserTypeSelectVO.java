package com.br.marketing.vo;

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class CustUserTypeSelectVO {

    @ApiModelProperty(value = "客户id(客户表中的主键id)")
    private String cid;

    @ApiModelProperty(value = "api_code")
    private String apiCode;

    @ApiModelProperty(value = "商户名称")
    private String name;

    @ApiModelProperty(value = "商户简称")
    private String shortName;

    @ApiModelProperty(value = "规则信息")
    private JSONObject conditionInfo;

    @ApiModelProperty(value = "是否适应全场景")
    private Integer allUserType;
}
