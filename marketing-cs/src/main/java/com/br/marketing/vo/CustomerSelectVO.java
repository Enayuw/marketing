package com.br.marketing.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 客户 cid、apiCode 信息vo
 *
 * @author zeqiang.guo@brgroup.com
 * @dateTime 2021/9/1 15:46
 */
@ApiModel(value = "cid、apiCode信息")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerSelectVO {
    /**
     * 2021/8/31 16:11 合作客户ID
     */
    @ApiModelProperty(value = "合作客户ID", dataType = "string")
    private String cid;
    /**
     * 2021/8/31 16:11 接口编码
     */
    @ApiModelProperty(value = "接口编码", dataType = "string", position = 1)
    private String apiCode;
}
