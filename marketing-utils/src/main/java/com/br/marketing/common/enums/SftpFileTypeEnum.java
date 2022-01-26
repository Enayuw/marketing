package com.br.marketing.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SftpFileTypeEnum {
    SEVEN("qiqi"),DX("dianxiao"),HLBYTRANSFORM("hl_transform");
   private String value;
}
