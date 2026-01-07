package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingHaloCallBackData;
import com.br.marketing.entity.MarketingHaloCallBackDataExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingHaloCallBackDataMapperBase {
    int countByExample(MarketingHaloCallBackDataExample example);

    int deleteByExample(MarketingHaloCallBackDataExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MarketingHaloCallBackData record);

    int insertSelective(MarketingHaloCallBackData record);

    List<MarketingHaloCallBackData> selectByExample(MarketingHaloCallBackDataExample example);

    MarketingHaloCallBackData selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MarketingHaloCallBackData record, @Param("example") MarketingHaloCallBackDataExample example);

    int updateByExample(@Param("record") MarketingHaloCallBackData record, @Param("example") MarketingHaloCallBackDataExample example);

    int updateByPrimaryKeySelective(MarketingHaloCallBackData record);

    int updateByPrimaryKey(MarketingHaloCallBackData record);
}