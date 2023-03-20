package com.br.marketing.service;

import java.time.LocalDate;

/**
 * 分期乐业务
 *
 * @author Guo Zeqiang
 * @dateTime 2023-03-13 10:08
 */
public interface IFenqileService {

    /**
     * 2023-03-13 10:40
     * 分期乐自动化周期转决策
     *
     * @param apiCode      客户编码
     * @param day          周期数
     * @param strategyCode 策略编号
     * @param localDate    本地日期
     * @param beginTimeStr 自定义开始时间
     * @param endTimeStr   自定义结束时间
     * @return 推送量级
     */
    Integer periodPushDecision(String apiCode, int day, String strategyCode, LocalDate localDate, String beginTimeStr, String endTimeStr);
}
