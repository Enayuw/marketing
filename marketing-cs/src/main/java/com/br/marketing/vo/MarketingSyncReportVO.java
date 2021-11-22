package com.br.marketing.vo;

import lombok.Data;


@Data
public class MarketingSyncReportVO {
    /**
     * 
     */
    private Long id;

    /**
     * 客户编号
     */
    private String cid;

    /**
     * 商户编号
     */
    private String apiCode;

    /**
     * 客户名称
     */
    private String shortName;

    /**
     * 上传日期
     */
    private String appletDate;

    /**
     * 场景
     */
    private String userType;

    /**
     * 数据正常入库条数
     */
    private Integer normalNum;

    /**
     * 去重后数据量
     */
    private Integer duplicateRemovalNum;

    /**
     * 上传开始时间
     */
    private String appletBeginTime;

    /**
     * 上传结束时间
     */
    private String appletEndTime;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private String createTime;

    /**
     * 修改时间
     */
    private String updateTime;

}