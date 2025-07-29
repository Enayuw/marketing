package com.br.marketing.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @ClassName QiFuRobotRankingReportData
 * @Author hang.zhou
 * @Date 2025/7/28
 */
@Data
public class QiFuAiRobotRankingReportData implements Serializable {

    /**
     * 短信发送率排名与第一名差距
     */
    private String smsRateRnGap;

    /**
     * 语音助手占比与自研差距
     */

    private String connectHRateZyGap;

    /**
     *静音占比与自研差距
     */
    private String  connectQRateZyGap;

    /**
     * 名单量
     */
    private String reachNum;

    /**
     * 人头登录率
     */
    private String userLoginRate;

    /**
     * 人头登录率排名
     */
    private String userLoginRateRn;

    /**
     * 人头登录率排名与第一名差距
     */
    private String userLoginRateRnGap;

    private List<BillReport> billReportList;

}
