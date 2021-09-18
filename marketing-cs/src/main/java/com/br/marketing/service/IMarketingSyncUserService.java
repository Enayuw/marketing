package com.br.marketing.service;

import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.vo.TodayIdTimeBySoleVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface IMarketingSyncUserService {

    /**
     * 获取有效去重的数据数量
     * @return
     */
    Long countRepeat(String execSql);

    /**
     * 获取当天有效的去重数据
     * @return
     */
    TodayIdTimeBySoleVo getSoleValidUser(String execSql);

    Integer updateRepeatUserStatus(String execSql);

}
