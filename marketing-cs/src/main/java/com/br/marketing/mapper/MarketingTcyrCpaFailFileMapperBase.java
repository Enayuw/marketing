package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingTcyrCpaFailFile;
import com.br.marketing.entity.MarketingTcyrCpaFailFileExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingTcyrCpaFailFileMapperBase {
    int countByExample(MarketingTcyrCpaFailFileExample example);

    int deleteByExample(MarketingTcyrCpaFailFileExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MarketingTcyrCpaFailFile record);

    int insertSelective(MarketingTcyrCpaFailFile record);

    List<MarketingTcyrCpaFailFile> selectByExample(MarketingTcyrCpaFailFileExample example);

    MarketingTcyrCpaFailFile selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MarketingTcyrCpaFailFile record, @Param("example") MarketingTcyrCpaFailFileExample example);

    int updateByExample(@Param("record") MarketingTcyrCpaFailFile record, @Param("example") MarketingTcyrCpaFailFileExample example);

    int updateByPrimaryKeySelective(MarketingTcyrCpaFailFile record);

    int updateByPrimaryKey(MarketingTcyrCpaFailFile record);
}