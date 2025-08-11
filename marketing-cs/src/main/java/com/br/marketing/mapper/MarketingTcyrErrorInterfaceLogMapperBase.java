package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingTcyrErrorInterfaceLog;
import com.br.marketing.entity.MarketingTcyrErrorInterfaceLogExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingTcyrErrorInterfaceLogMapperBase {
    int countByExample(MarketingTcyrErrorInterfaceLogExample example);

    int deleteByExample(MarketingTcyrErrorInterfaceLogExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MarketingTcyrErrorInterfaceLog record);

    int insertSelective(MarketingTcyrErrorInterfaceLog record);

    List<MarketingTcyrErrorInterfaceLog> selectByExample(MarketingTcyrErrorInterfaceLogExample example);

    MarketingTcyrErrorInterfaceLog selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MarketingTcyrErrorInterfaceLog record, @Param("example") MarketingTcyrErrorInterfaceLogExample example);

    int updateByExample(@Param("record") MarketingTcyrErrorInterfaceLog record, @Param("example") MarketingTcyrErrorInterfaceLogExample example);

    int updateByPrimaryKeySelective(MarketingTcyrErrorInterfaceLog record);

    int updateByPrimaryKey(MarketingTcyrErrorInterfaceLog record);
}