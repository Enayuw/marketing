package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingTransferInfo;
import com.br.marketing.entity.MarketingTransferInfoExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingTransferInfoMapperBase {
    int countByExample(MarketingTransferInfoExample example);

    int deleteByExample(MarketingTransferInfoExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MarketingTransferInfo record);

    int insertSelective(MarketingTransferInfo record);

    List<MarketingTransferInfo> selectByExample(MarketingTransferInfoExample example);

    MarketingTransferInfo selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MarketingTransferInfo record, @Param("example") MarketingTransferInfoExample example);

    int updateByExample(@Param("record") MarketingTransferInfo record, @Param("example") MarketingTransferInfoExample example);

    int updateByPrimaryKeySelective(MarketingTransferInfo record);

    int updateByPrimaryKey(MarketingTransferInfo record);
}