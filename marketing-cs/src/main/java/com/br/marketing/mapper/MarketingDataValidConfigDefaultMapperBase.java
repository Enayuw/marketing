package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingDataValidConfigDefault;
import com.br.marketing.entity.MarketingDataValidConfigDefaultExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface MarketingDataValidConfigDefaultMapperBase {
    long countByExample(MarketingDataValidConfigDefaultExample example);

    int deleteByExample(MarketingDataValidConfigDefaultExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MarketingDataValidConfigDefault record);

    int insertSelective(MarketingDataValidConfigDefault record);

    List<MarketingDataValidConfigDefault> selectByExample(MarketingDataValidConfigDefaultExample example);

    MarketingDataValidConfigDefault selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MarketingDataValidConfigDefault record, @Param("example") MarketingDataValidConfigDefaultExample example);

    int updateByExample(@Param("record") MarketingDataValidConfigDefault record, @Param("example") MarketingDataValidConfigDefaultExample example);

    int updateByPrimaryKeySelective(MarketingDataValidConfigDefault record);

    int updateByPrimaryKey(MarketingDataValidConfigDefault record);
}