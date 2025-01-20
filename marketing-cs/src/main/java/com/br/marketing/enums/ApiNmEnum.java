package com.br.marketing.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ApiNmEnum {
    ZANPUSHDETAIL("zanPushDetail","众安上报"),
    ZANZK("zanZk","众安肄业撞库"),
    CARCLUECOMMIT("carClueCommit","车线索提交")
    ;

    private String apiNm;

    private String apiDesc;

}
