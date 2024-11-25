package com.br.marketing.client.robotaiapi.input;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 为了扩展ConversionData类中字段，新增本类，以便在规则处理中使用方便
 * Auto-generated: 2021-08-04 10:58:58
 */
@Getter
@Setter
@ToString
public class ConversionDataDTO extends ConversionData {
    /**
     * apiCode
     */
    private String apiCode;
}