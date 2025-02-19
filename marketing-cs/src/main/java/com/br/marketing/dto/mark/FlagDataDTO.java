package com.br.marketing.dto.mark;

import lombok.Data;

/**
 * @ClassName FlagDataDTO
 * @Author kongbx
 * @Date 2025/2/19 20:21
 */
@Data
public class FlagDataDTO {

    private Integer md5Phone;

    private Integer flagNewCust;

    private String flagRiskgroup;
    private Integer flagInterest;

    private Integer flagAge;
    private Integer flagProvince;
    private Integer flagSpecialSmall;
    private Integer flagSpecialrisklevel;
    private Integer flagIndexcs;
    private Integer flagApplyloan;

    private Integer flagIntellaudioBlacklist;
    private Integer flagWithoutWillingness;

    private Integer flagScoreWhitelist;
    private Integer flagWhitelist;

    private Integer flagScoreysbase;
    private Integer flagScorefxsbbaseb;
    private Integer flagScorescashonregisternologin;
    private Integer flagScorescashonyxxy;
    private Integer flagScorencashonzawswyyym;
    private Integer pdIdApplyAge;
    private Integer pdCellApplyAge;
    private Integer kaIdProvince;
    private Integer kaCellProvince;
    private Integer slIdNbankBadAllnum;
    private Integer slCellNbankBadAllnum;
    private Integer slCellNbankOverdueTime ;
    private Integer slCellNbankBad;
    private Integer slIdNbankBad;
    private Integer slIdNbankBadTime;
    private Integer slIdNbankNsloanOverdueTime;
    private Integer indexcs;
    private Integer alsM1IdNbankOrgnum;
    private Integer alsM3IdNbankOrgnum;
    private Integer alsM1CellNbankOrgnum;
    private Integer alsM3CellNbankOrgnum;
    private Integer scoreysbase;
    private Integer scorefxsbbaseb;
    private Integer scorescashonregisternologin;
    private Integer scorescashonyxxy;
    private Integer scorencashonzawswyyym;

}
