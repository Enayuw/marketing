package com.br.marketing.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SftpFileTypeEnum {
    SEVEN("qiqi")
    ,DX("dianxiao")
    ,HLBYTRANSFORM("hl_transform")
    ,SHBYTRANSFORM("sh_transform")
    ,DXTRANSFORM("dx_transform")
    ,DXIBU("dx_ibu")
    ,ZHONGBANGLABEL("zhongbanglabel")
    ,XIECHENGSMSQUIT("xiechengsms")
    ,DD("didi");
   private String value;
}
