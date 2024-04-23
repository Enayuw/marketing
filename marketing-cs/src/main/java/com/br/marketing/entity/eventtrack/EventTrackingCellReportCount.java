package com.br.marketing.entity.eventtrack;

import lombok.Data;

/**
 * 埋点统计给前端展示【日志记录-日志统计】类
 * @Author yu.xia@brgroup.com
 * @Date 2024/4/17 15:54
 */
@Data
public class EventTrackingCellReportCount {

    /**
     * 查询用户-登陆用户名
     */
    private String userName;

    /**
     * 查询日期-创建日期
     */
    private String createTime;

    /**
     * 日维度统计次数
     */
    private Long count;

}