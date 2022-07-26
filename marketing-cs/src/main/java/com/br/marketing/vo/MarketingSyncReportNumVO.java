package com.br.marketing.vo;

import lombok.Data;
/**
 *上传记录-统计总数
 */
@Data
public class MarketingSyncReportNumVO {


    private Integer normalNumTotal;


    private Integer duplicateRemovalNumTotal;


    private String apiCode;


}
