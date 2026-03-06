package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingDataFileConfigNoHeader;
import com.br.marketing.entity.MarketingDataFileConfigNoHeaderExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingDataFileConfigNoHeaderMapperBase {
    int countByExample(MarketingDataFileConfigNoHeaderExample example);

    int deleteByExample(MarketingDataFileConfigNoHeaderExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MarketingDataFileConfigNoHeader record);

    int insertSelective(MarketingDataFileConfigNoHeader record);

    List<MarketingDataFileConfigNoHeader> selectByExample(MarketingDataFileConfigNoHeaderExample example);

    MarketingDataFileConfigNoHeader selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MarketingDataFileConfigNoHeader record,
                                 @Param("example") MarketingDataFileConfigNoHeaderExample example);

    int updateByExample(@Param("record") MarketingDataFileConfigNoHeader record,
                        @Param("example") MarketingDataFileConfigNoHeaderExample example);

    int updateByPrimaryKeySelective(MarketingDataFileConfigNoHeader record);

    int updateByPrimaryKey(MarketingDataFileConfigNoHeader record);
}