package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingJsonNodeParse;
import com.br.marketing.entity.MarketingJsonNodeParseExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface MarketingJsonNodeParseMapperBase {
    long countByExample(MarketingJsonNodeParseExample example);

    int deleteByExample(MarketingJsonNodeParseExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MarketingJsonNodeParse record);

    int insertSelective(MarketingJsonNodeParse record);

    List<MarketingJsonNodeParse> selectByExample(MarketingJsonNodeParseExample example);

    MarketingJsonNodeParse selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MarketingJsonNodeParse record, @Param("example") MarketingJsonNodeParseExample example);

    int updateByExample(@Param("record") MarketingJsonNodeParse record, @Param("example") MarketingJsonNodeParseExample example);

    int updateByPrimaryKeySelective(MarketingJsonNodeParse record);

    int updateByPrimaryKey(MarketingJsonNodeParse record);
}