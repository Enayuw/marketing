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

    String getUserTypeLatestByCustNum(String apiCode, String custNum);

}
