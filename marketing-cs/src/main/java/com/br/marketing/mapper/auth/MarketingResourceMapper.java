package com.br.marketing.mapper.auth;

import com.br.marketing.entity.auth.MarketingResource;
import com.br.marketing.entity.auth.MarketingResourceExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingResourceMapper {
    int countByExample(MarketingResourceExample example);

    int deleteByExample(MarketingResourceExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(MarketingResource record);

    int insertSelective(MarketingResource record);

    List<MarketingResource> selectByExample(MarketingResourceExample example);

    MarketingResource selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") MarketingResource record, @Param("example") MarketingResourceExample example);

    int updateByExample(@Param("record") MarketingResource record, @Param("example") MarketingResourceExample example);

    int updateByPrimaryKeySelective(MarketingResource record);

    int updateByPrimaryKey(MarketingResource record);
    List<MarketingResource> getResourcesByUid(Integer userId);

}