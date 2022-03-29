package com.br.marketing.mapper.auth;

import com.br.marketing.entity.auth.MarketingRoleResource;
import com.br.marketing.entity.auth.MarketingRoleResourceExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingRoleResourceMapper {
    int countByExample(MarketingRoleResourceExample example);

    int deleteByExample(MarketingRoleResourceExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(MarketingRoleResource record);

    int insertSelective(MarketingRoleResource record);

    List<MarketingRoleResource> selectByExample(MarketingRoleResourceExample example);

    MarketingRoleResource selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") MarketingRoleResource record, @Param("example") MarketingRoleResourceExample example);

    int updateByExample(@Param("record") MarketingRoleResource record, @Param("example") MarketingRoleResourceExample example);

    int updateByPrimaryKeySelective(MarketingRoleResource record);

    int updateByPrimaryKey(MarketingRoleResource record);
}