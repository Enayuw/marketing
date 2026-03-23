package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingCleanHeaderTableMapping;
import com.br.marketing.entity.MarketingCleanHeaderTableMappingExample;

import java.util.List;

import org.apache.ibatis.annotations.Param;

public interface MarketingCleanHeaderTableMappingMapperBase {
    long countByExample(MarketingCleanHeaderTableMappingExample example);

    int deleteByExample(MarketingCleanHeaderTableMappingExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MarketingCleanHeaderTableMapping record);

    int insertSelective(MarketingCleanHeaderTableMapping record);

    List<MarketingCleanHeaderTableMapping> selectByExample(MarketingCleanHeaderTableMappingExample example);

    MarketingCleanHeaderTableMapping selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MarketingCleanHeaderTableMapping record,
                                 @Param("example") MarketingCleanHeaderTableMappingExample example);

    int updateByExample(@Param("record") MarketingCleanHeaderTableMapping record, @Param("example") MarketingCleanHeaderTableMappingExample example);

    int updateByPrimaryKeySelective(MarketingCleanHeaderTableMapping record);

    int updateByPrimaryKey(MarketingCleanHeaderTableMapping record);
}
