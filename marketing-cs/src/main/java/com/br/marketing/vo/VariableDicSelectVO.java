package com.br.marketing.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 客户配置变量值字典 下拉列表vo
 *
 * @author zeqiang.guo@brgroup.com
 * @dateTime 2021/9/1 17:49
 */
@Data
@Schema(description = "客户配置变量值字典")
public class VariableDicSelectVO implements Serializable {

    private static final long serialVersionUID = 3260454161828573655L;
    /**
     * 字段名称
     */
    @Schema(description = "字段名称")
    private String fieldName;

    /**
     * 字段值
     */
    @Schema(description = "字段值")
    private String fieldValue;

    /**
     * 字段描述
     */
    @Schema(description = "字段描述")
    private String fieldDesc;

    public VariableDicSelectVO(String fieldName, String fieldValue, String fieldDesc) {
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
        this.fieldDesc = fieldDesc;
    }

    public VariableDicSelectVO() {
    }
}
