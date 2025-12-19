package com.br.marketing.dto.autocheck;

import lombok.Data;

@Data
public class CheckUploadSyncDataDto {

    private String apiCode;

    /**
     * 数据正常入库条数
     */
    private Integer normalNum;

    /**
     * 去重后数据量
     */
    private Integer duplicateRemovalNum;

    /**
     * 快照时间：最新快照的生成时间
     */
    private String snapTime;
}
