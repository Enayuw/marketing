package com.br.marketing.client.biocloo.input;

import com.br.marketing.rule.SourceData;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class DataDTO extends SourceData {
    @Schema(description = "apiCode")
    private String apiCode;
    @Schema(description = "姓名 ")
    private String name;
    @Schema(description = "手机号")
    private String phone;
    @Schema(description = "案件编号")
    private String caseNum;
    @Schema(description = "生效开始时间")
    private String effectiveDate;
    @Schema(description = "生效截止时间")
    private String expireDate;
    @Schema(description = "备注")
    private String remark;
}
