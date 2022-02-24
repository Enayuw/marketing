package com.br.marketing.mapper;


import com.br.marketing.entity.MarketingTransferSyncUser;
import org.apache.ibatis.annotations.Param;

public interface MarketingTransferSyncUserMapper extends MarketingTransferSyncUserMapperBase {
    /**
     * 根据cust_num获取最新数据
     * @param cid
     * @param caseNum
     * @return
     */
    MarketingTransferSyncUser getNewestByCusnum(@Param("cid") String cid, @Param("caseNum") String caseNum);
}