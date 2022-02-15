package com.br.marketing.service;

import com.br.marketing.vo.TodayIdTimeBySoleVo;

import java.util.Date;

public interface IMarketingSyncUserService {

    /**
     * 获取有效去重的数据数量
     *
     * @return
     */
    Long countRepeat(String execSql);

    /**
     * 获取当天有效的去重数据
     *
     * @return
     */
    TodayIdTimeBySoleVo getSoleValidUser(String execSql);

    Integer updateRepeatUserStatus(String execSql);

    /**
     * 根据案件编号获取客户最新的场景
     *
     * @param apiCode apiCode
     * @param custNum 案件编号
     * @return userType
     * @author Guo Zeqiang
     * @dateTime 2022/2/11 18:17
     */
    String getUserTypeLatestByCustNum(String apiCode, String custNum);

    /**
     * 根据案件编号获取最新的taskId
     *
     * @param apiCode apiCode
     * @param custNum 案件编号
     * @return taskId
     * @author Guo Zeqiang
     * @dateTime 2022/2/15 10:52
     */
    String getTaskIdLatestByCustNum(String apiCode, String custNum);


    /**
     * 获取案件编号的上传时间
     *
     * @param apiCode  apiCode
     * @param custNum  案件编号
     * @param userType 场景
     * @return AppletTime
     * @author Guo Zeqiang
     * @dateTime 2022/2/15 10:52
     */
    Date getAppletTimeByCustNumAndUserType(String apiCode, String custNum, String userType);
}
