package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingTcyrCpaSuccessData;
import com.br.marketing.entity.MarketingTcyrCpaSuccessDataExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingTcyrCpaSuccessDataMapperBase {
    int countByExample(MarketingTcyrCpaSuccessDataExample example);

    int deleteByExample(MarketingTcyrCpaSuccessDataExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MarketingTcyrCpaSuccessData record);

    int insertSelective(MarketingTcyrCpaSuccessData record);

    List<MarketingTcyrCpaSuccessData> selectByExample(MarketingTcyrCpaSuccessDataExample example);

    MarketingTcyrCpaSuccessData selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MarketingTcyrCpaSuccessData record, @Param("example") MarketingTcyrCpaSuccessDataExample example);

    int updateByExample(@Param("record") MarketingTcyrCpaSuccessData record, @Param("example") MarketingTcyrCpaSuccessDataExample example);

    int updateByPrimaryKeySelective(MarketingTcyrCpaSuccessData record);

    int updateByPrimaryKey(MarketingTcyrCpaSuccessData record);
}