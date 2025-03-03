package com.br.marketing.dto.mark;

import lombok.Data;

/**
 * @ClassName FlagDataWriteBackFileMark
 * @Author kongbx
 * @Date 2025/2/26 23:12
 */
@Data
public class FlagDataWriteBackFileMark {
    private String requestTime ;
    private String batchNumber;
    private String cusNum;
    private String strategyId;
    private String version;
    private String taskId;
    private String userType ;
    private String cell;
    private String createTime ;
    private Integer flagNewCust;
    private String flagRiskgroup;
    private Integer flagInterest;
    private String dtWhitelist ;
    private Integer flagAge;
    private Integer flagProvince;
    private Integer flagSpecialSmall;
    private Integer flagSpecialrisklevelRule;
    private Integer flagApplyloan ;
    private Integer flagIntellaudioBlacklist;
    private Integer flagWithoutWillingness;
    private Integer flagWhitelist;
    private Integer flagScoreysbase;
    private Integer flagScorefxsbbaseb ;
    private Integer flagScorescashonregisternologin ;
    private Integer flagScorescashonyxxy;
    private Integer flagScorencashonzawswyyym ;
    private String pdIdApplyAge ;
    private String pdCellApplyAge ;
    private String kaIdProvince;
    private String kaCellProvince;
    private String flagSpecialrisklevel;
    private String slIdNbankBadAllnum ;
    private String slCellNbankBadAllnum;
    private String slCellNbankOverdueTime ;
    private String slCellNbankBad ;
    private String slIdNbankBad ;
    private String slIdNbankBadTime;
    private String slIdNbankNsloanOverdueTime;
    private Integer alsM1IdNbankOrgnum;
    private Integer alsM3IdNbankOrgnum;
    private Integer alsM1CellNbankOrgnum;
    private Integer alsM3CellNbankOrgnum;
    private Integer scorencashonxctx;
    private Integer scoreysbase;
    private Integer scorefxsbbaseb ;
    private Integer scorescashonregisternologin;
    private Integer scorescashonyxxy;
    private Integer scorencashonzawswyyym;
    
}
