package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingTcyrCpaFile;
import com.br.marketing.entity.MarketingTcyrCpaFileExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingTcyrCpaFileMapperBase {
    int countByExample(MarketingTcyrCpaFileExample example);

    int deleteByExample(MarketingTcyrCpaFileExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MarketingTcyrCpaFile record);

    int insertSelective(MarketingTcyrCpaFile record);

    List<MarketingTcyrCpaFile> selectByExample(MarketingTcyrCpaFileExample example);

    MarketingTcyrCpaFile selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MarketingTcyrCpaFile record, @Param("example") MarketingTcyrCpaFileExample example);

    int updateByExample(@Param("record") MarketingTcyrCpaFile record, @Param("example") MarketingTcyrCpaFileExample example);

    int updateByPrimaryKeySelective(MarketingTcyrCpaFile record);

    int updateByPrimaryKey(MarketingTcyrCpaFile record);
}