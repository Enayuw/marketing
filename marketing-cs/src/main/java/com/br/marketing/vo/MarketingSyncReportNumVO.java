package com.br.marketing.vo;

import lombok.Data;
/**
 *上传记录-统计总数
 */
@Data
public class MarketingSyncReportNumVO {


    private Long normalNumTotal;


    private Long duplicateRemovalNumTotal;


    private String apiCode;


}
