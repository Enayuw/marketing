package com.br.marketing.dto.autocheck;

import lombok.Data;

@Data
public class CheckUploadSyncDataDto {

    private String apiCode;

    /**
     * 客户编号
     */
    private String cid;

    /**
     * 公司名称
     */
    private String shortName;

    /**
     * 场景
     */
    private String userType;

    /**
     * 扩展字段中key的集合
     */
    private String reserveField1Key;

    /**
     * 备注
     */
    private String remark;

    /**
     * 标签信息json结构
     */
    private String labelMessage;

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
