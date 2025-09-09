package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingTcyrCpaPushFileTask;
import com.br.marketing.entity.MarketingTcyrCpaPushFileTaskExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingTcyrCpaPushFileTaskMapperBase {
    int countByExample(MarketingTcyrCpaPushFileTaskExample example);

    int deleteByExample(MarketingTcyrCpaPushFileTaskExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MarketingTcyrCpaPushFileTask record);

    int insertSelective(MarketingTcyrCpaPushFileTask record);

    List<MarketingTcyrCpaPushFileTask> selectByExample(MarketingTcyrCpaPushFileTaskExample example);

    MarketingTcyrCpaPushFileTask selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MarketingTcyrCpaPushFileTask record, @Param("example") MarketingTcyrCpaPushFileTaskExample example);

    int updateByExample(@Param("record") MarketingTcyrCpaPushFileTask record, @Param("example") MarketingTcyrCpaPushFileTaskExample example);

    int updateByPrimaryKeySelective(MarketingTcyrCpaPushFileTask record);

    int updateByPrimaryKey(MarketingTcyrCpaPushFileTask record);
}