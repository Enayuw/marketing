package com.br.marketing.dto.autocheck;

import lombok.Data;

@Data
public class CheckTransferSyncDataDto {

    private String apiCode;

    /**
     * 转化数据量
     */

    private Integer dataCount;

    /**
     * 快照时间：前一天08:00快照/最新快照的生成时间
     */
    private String snapTime;
}
