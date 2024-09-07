package com.br.marketing.client.biocloo.input;

import com.br.marketing.rule.InterfaceParams;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class BlackDataDTO extends InterfaceParams {

    @ApiModelProperty(value = "请求类型:固定值blackData")
    private String method;
    @ApiModelProperty(value = "apiCode")
    private String apiCode;
    @ApiModelProperty(value = "黑名单列表")
    private List<DataDTO> data;

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class DataDTO extends InterfaceParams {

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
    }

}
