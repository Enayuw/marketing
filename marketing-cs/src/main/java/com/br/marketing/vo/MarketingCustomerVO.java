package com.br.marketing.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

@Data
public class MarketingCustomerVO {

    /**
     * 主键id
     */
    @Schema(description = "主键id")
    private String id;

    /**
     * api_code
     */
    @Schema(description = "api_code")
    private String apiCode;

    /**
     * 合作客户ID
     */
    @Schema(description = "合作客户ID")
    @NotEmpty(message = "客户id必传")
    private String cid;

    /**
     * 合作客户名称
     */
    @Schema(description = "合作客户名称")
    private String name;

    /**
     * 合作客户简称
     */
    @Schema(description = "合作客户简称")
    private String shortName;

    /**
     * 跑分批次进度 Redis 保留天数（天，varchar）
     */
    @Schema(description = "跑分批次进度 Redis 保留天数（天）")
    private String expireDay;


}
