package com.br.marketing.dto.mark;

import lombok.Data;

/**
 * @ClassName FlagDataEsMark
 * @Author kongbx
 * @Date 2025/2/19 20:21
 */
@Data
public class FlagDataEsMark {

    private Long id;
    private String cellLog;

    private Integer flagNewCust;

    private String flagRiskgroup;
    private Integer flagInterest;

    private Integer flagAge;
    private Integer flagProvince;
    private Integer flagSpecialSmall;
    private Integer flagSpecialrisklevel;
    private Integer flagIndexcs;
    private Integer flagApplyloan;

    private Integer flagScoreysbase;
    private Integer flagScorefxsbbaseb;
    private Integer flagScorescashonregisternologin;
    private Integer flagScorescashonyxxy;
    private Integer flagScorencashonzawswyyym;

    private Integer flagIntellaudioBlacklist;
    private Integer flagWithoutWillingness;

    private Integer flagWhitelist;

}
