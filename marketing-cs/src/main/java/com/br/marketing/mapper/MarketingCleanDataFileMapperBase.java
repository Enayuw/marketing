package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingCleanDataFile;
import com.br.marketing.entity.MarketingCleanDataFileExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface MarketingCleanDataFileMapperBase {
    long countByExample(MarketingCleanDataFileExample example);

    int deleteByExample(MarketingCleanDataFileExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MarketingCleanDataFile record);

    int insertSelective(MarketingCleanDataFile record);

    List<MarketingCleanDataFile> selectByExample(MarketingCleanDataFileExample example);

    MarketingCleanDataFile selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MarketingCleanDataFile record, @Param("example") MarketingCleanDataFileExample example);

    int updateByExample(@Param("record") MarketingCleanDataFile record, @Param("example") MarketingCleanDataFileExample example);

    int updateByPrimaryKeySelective(MarketingCleanDataFile record);

    int updateByPrimaryKey(MarketingCleanDataFile record);
}