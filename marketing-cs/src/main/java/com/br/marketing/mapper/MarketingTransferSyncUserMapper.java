package com.br.marketing.mapper;


import com.br.marketing.entity.MarketingTransferSyncUser;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingTransferSyncUserMapper extends MarketingTransferSyncUserMapperBase {
    /**
     * 根据cust_num获取最新数据
     * @param cid
     * @param caseNum
     * @return
     */
    MarketingTransferSyncUser getNewestByCusnum(@Param("cid") String cid, @Param("caseNum") String caseNum);

    /**
     * 根据cust_num、Apicode获取1h内最新数据
     */
    MarketingTransferSyncUser getNewestByCusnumAndApicode(@Param("cid") String cid, @Param("caseNum") String caseNum, @Param("apicode") String apicode
            , @Param("userType") String userType,@Param("timeAddHour") String timeAddHour);

    /**
     * 根据cust_num获取1小时内最新数据
     * @param cid
     * @param caseNum
     * @param timeAddHour
     * @return
     */
    MarketingTransferSyncUser getNewestByCusnumInHour(@Param("cid") String cid, @Param("caseNum") String caseNum, @Param("timeAddHour") String timeAddHour);

    List<MarketingTransferSyncUser> getTransferOrderInsertTime(@Param("cid") String cid,@Param("data") String data,@Param("limitStart") Integer limitStart);

    /**
     * 获取指定日期，指定custNum的非延时数据
     * @param cid
     * @param custNums
     * @param date
     * @return
     */
    List<MarketingTransferSyncUser> getTransferOrderInsertTimeByCustNum(@Param("cid") String cid,@Param("custNums")List<String> custNums ,@Param("date") String date);

    /**
     * 获取指定custNum的最新数据
     * @param cid
     * @param custNums
     * @param date
     * @return
     */
    List<MarketingTransferSyncUser> getTransferOrderRequestTimeByCustNum(@Param("cid") String cid,@Param("custNums")List<String> custNums ,@Param("date") String date);
}