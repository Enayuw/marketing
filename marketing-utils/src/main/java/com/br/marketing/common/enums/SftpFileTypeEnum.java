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
    ,TONGCHENG_UNDO_PUSHTOCUSTOMER("tongcheng_undo_pushToCustomer")
    ,TONGCHENGGROUP_OPERATION_PUSHTOCUSTOMER("tongcheng_operation_pushToCustomer")
    ,XIECHENGSMSQUIT("xiechengsms")
    ,DD("didi");
   private String value;
}
