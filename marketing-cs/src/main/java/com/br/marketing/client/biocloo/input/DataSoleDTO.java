package com.br.marketing.client.biocloo.input;

import com.br.marketing.rule.SourceData;
import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(description = "情况类型:0:黑名单;1:转化")
    private String status;
    @Schema(description = "数据id")
    private String dataId;

}
