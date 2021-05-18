package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingDirtyUser;
import com.br.marketing.entity.MarketingDirtyUserExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingDirtyUserMapperBase {
    int countByExample(MarketingDirtyUserExample example);

    int deleteByExample(MarketingDirtyUserExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(MarketingDirtyUser record);

    int insertSelective(MarketingDirtyUser record);

    List<MarketingDirtyUser> selectByExample(MarketingDirtyUserExample example);

    MarketingDirtyUser selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") MarketingDirtyUser record, @Param("example") MarketingDirtyUserExample example);

    int updateByExample(@Param("record") MarketingDirtyUser record, @Param("example") MarketingDirtyUserExample example);

    int updateByPrimaryKeySelective(MarketingDirtyUser record);

    int updateByPrimaryKey(MarketingDirtyUser record);
}