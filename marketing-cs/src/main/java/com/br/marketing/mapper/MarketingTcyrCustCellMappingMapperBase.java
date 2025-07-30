package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingTcyrCustCellMapping;
import com.br.marketing.entity.MarketingTcyrCustCellMappingExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingTcyrCustCellMappingMapperBase {
    int countByExample(MarketingTcyrCustCellMappingExample example);

    int deleteByExample(MarketingTcyrCustCellMappingExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MarketingTcyrCustCellMapping record);

    int insertSelective(MarketingTcyrCustCellMapping record);

    List<MarketingTcyrCustCellMapping> selectByExample(MarketingTcyrCustCellMappingExample example);

    MarketingTcyrCustCellMapping selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MarketingTcyrCustCellMapping record, @Param("example") MarketingTcyrCustCellMappingExample example);

    int updateByExample(@Param("record") MarketingTcyrCustCellMapping record, @Param("example") MarketingTcyrCustCellMappingExample example);

    int updateByPrimaryKeySelective(MarketingTcyrCustCellMapping record);

    int updateByPrimaryKey(MarketingTcyrCustCellMapping record);
}