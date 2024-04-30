package com.br.marketing.vo;

import lombok.Data;

@Data
public class FileToMarketingFieldVO {
    private String headField;
    private String interfaceField;
    private Boolean isMust;
    private String defalutValue;
    private Boolean isExtend;
    private String dynamicData;
    private String conversion;
}
