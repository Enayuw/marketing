package com.br.marketing.service;

import com.br.marketing.vo.TodayIdTimeBySoleVo;

public interface IMarketingSyncUserService {

    /**
     * 获取有效去重的数据数量
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
}
