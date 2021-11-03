package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUserExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingTransferSyncUserMapperBase {
    int countByExample(MarketingTransferSyncUserExample example);

    int deleteByExample(MarketingTransferSyncUserExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MarketingTransferSyncUser record);

    int insertSelective(MarketingTransferSyncUser record);

    List<MarketingTransferSyncUser> selectByExample(MarketingTransferSyncUserExample example);

    MarketingTransferSyncUser selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MarketingTransferSyncUser record, @Param("example") MarketingTransferSyncUserExample example);

    int updateByExample(@Param("record") MarketingTransferSyncUser record, @Param("example") MarketingTransferSyncUserExample example);

    int updateByPrimaryKeySelective(MarketingTransferSyncUser record);

    int updateByPrimaryKey(MarketingTransferSyncUser record);
}