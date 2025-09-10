package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingTcyrCpaFailData;
import com.br.marketing.entity.MarketingTcyrCpaFailDataExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingTcyrCpaFailDataMapperBase {
    int countByExample(MarketingTcyrCpaFailDataExample example);

    int deleteByExample(MarketingTcyrCpaFailDataExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MarketingTcyrCpaFailData record);

    int insertSelective(MarketingTcyrCpaFailData record);

    List<MarketingTcyrCpaFailData> selectByExampleWithBLOBs(MarketingTcyrCpaFailDataExample example);

    List<MarketingTcyrCpaFailData> selectByExample(MarketingTcyrCpaFailDataExample example);

    MarketingTcyrCpaFailData selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MarketingTcyrCpaFailData record, @Param("example") MarketingTcyrCpaFailDataExample example);

    int updateByExampleWithBLOBs(@Param("record") MarketingTcyrCpaFailData record, @Param("example") MarketingTcyrCpaFailDataExample example);

    int updateByExample(@Param("record") MarketingTcyrCpaFailData record, @Param("example") MarketingTcyrCpaFailDataExample example);

    int updateByPrimaryKeySelective(MarketingTcyrCpaFailData record);

    int updateByPrimaryKeyWithBLOBs(MarketingTcyrCpaFailData record);

    int updateByPrimaryKey(MarketingTcyrCpaFailData record);
}