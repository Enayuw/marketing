package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingTcyrCpaSuccessFile;
import com.br.marketing.entity.MarketingTcyrCpaSuccessFileExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingTcyrCpaSuccessFileMapperBase {
    int countByExample(MarketingTcyrCpaSuccessFileExample example);

    int deleteByExample(MarketingTcyrCpaSuccessFileExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MarketingTcyrCpaSuccessFile record);

    int insertSelective(MarketingTcyrCpaSuccessFile record);

    List<MarketingTcyrCpaSuccessFile> selectByExample(MarketingTcyrCpaSuccessFileExample example);

    MarketingTcyrCpaSuccessFile selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MarketingTcyrCpaSuccessFile record, @Param("example") MarketingTcyrCpaSuccessFileExample example);

    int updateByExample(@Param("record") MarketingTcyrCpaSuccessFile record, @Param("example") MarketingTcyrCpaSuccessFileExample example);

    int updateByPrimaryKeySelective(MarketingTcyrCpaSuccessFile record);

    int updateByPrimaryKey(MarketingTcyrCpaSuccessFile record);
}