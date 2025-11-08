package com.br.marketing.vo;

import com.alibaba.fastjson.JSONObject;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class CustUserTypeSelectVO {

    @Schema(description = "客户id(客户表中的主键id)")
    private String cid;

    @Schema(description = "api_code")
    private String apiCode;

    @Schema(description = "商户名称")
    private String name;

    @Schema(description = "商户简称")
    private String shortName;

    @Schema(description = "规则信息")
    private JSONObject conditionInfo;

    @Schema(description = "是否适应全场景")
    private Integer allUserType;
}
