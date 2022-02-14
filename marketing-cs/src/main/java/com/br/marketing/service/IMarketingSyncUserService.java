package com.br.marketing.service;

import com.br.marketing.dos.PeriodOfValidityDO;
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
     * 是否在有效期内
     *
     * @param apiCode            apiCode
     * @param custNum            案件编号
     * @param periodOfValidityDO 有效期pojo
     * @return true or false ,在有效期间为true，否则为false
     * @author Guo Zeqiang
     * @dateTime 2022/2/14 9:58
     */
    Boolean isPeriodOfValidity(String apiCode, String custNum, PeriodOfValidityDO periodOfValidityDO);

}
