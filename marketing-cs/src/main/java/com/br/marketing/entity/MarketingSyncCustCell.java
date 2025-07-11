package com.br.marketing.entity;

import lombok.Data;


@Data
public class MarketingSyncCustCell {
    private Long id;

    /**
     * 用户编号
     */
    private String custNum;

    /**
     * 手机号md5
     */
    private String cellMd5;
}
