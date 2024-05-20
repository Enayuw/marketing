package com.br.marketing.entity.eventtrack;

import lombok.Data;

/**
 * 埋点统计给前端展示【日志记录-日志统计】类
 * @Author yu.xia@brgroup.com
 * @Date 2024/4/17 15:55
 */
@Data
public class EventTrackingCellReportDetail {

    /**
     * 查询日期-创建时间 yyyy-MM-dd
     */
    private String createDate;

    /**
     * 查询用户-登陆用户名
     */
    private String userName;

    /**
     * 使用手机号查询时入参：cid或用户名
     */
    private String cidOrName;
    /**
     * apiCode对应的cid
     */
    private String cid;
    /**
     * apiCode对应的shortName
     */
    private String shortName;
    /**
     * 使用手机号查询时入参：apiCode
     */
    private String apiCodes;
    /**
     * 使用手机号查询时入参：cell
     */
    private String cell;
    /**
     * 查询日期-创建时间 yyyy-MM-dd HH:mm:ss
     */
    private String createTime;

}