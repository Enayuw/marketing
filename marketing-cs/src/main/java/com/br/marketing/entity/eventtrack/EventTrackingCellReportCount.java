package com.br.marketing.entity.eventtrack;

import lombok.Data;

import java.util.Date;

@Data
public class EventTrackingCellReportCount {

    /**
     * 登陆用户名
     */
    private String userName;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 日维度统计次数
     */
    private int count;

}