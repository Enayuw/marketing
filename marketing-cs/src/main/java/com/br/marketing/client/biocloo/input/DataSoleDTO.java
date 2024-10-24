package com.br.marketing.client.biocloo.input;

import com.br.marketing.rule.SourceData;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 黑名单规则内去重DTO
 *
 * @author senyang.zheng
 * @date 2024/09/09
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DataSoleDTO extends SourceData {

    @ApiModelProperty(value = "apiCode")
    private String apiCode;
    @ApiModelProperty(value = "姓名 ")
    private String name;
    @ApiModelProperty(value = "手机号")
    private String phone;
    @ApiModelProperty(value = "案件编号")
    private String caseNum;
    @ApiModelProperty(value = "生效开始时间")
    private String effectiveDate;
    @ApiModelProperty(value = "生效截止时间")
    private String expireDate;
    @ApiModelProperty(value = "备注")
    private String remark;
    @ApiModelProperty(value = "情况类型:0:黑名单;1:转化")
    private String status;
    @ApiModelProperty(value = "数据id")
    private String dataId;

}
