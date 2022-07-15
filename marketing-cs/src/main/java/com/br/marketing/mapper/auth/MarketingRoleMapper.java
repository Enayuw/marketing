package com.br.marketing.mapper.auth;

import com.br.marketing.entity.auth.MarketingRole;
import com.br.marketing.entity.auth.MarketingRoleExample;
import com.br.marketing.mysqlInterceptor.AddDataAuth;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingRoleMapper {
    int countByExample(MarketingRoleExample example);

    int deleteByExample(MarketingRoleExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(MarketingRole record);

    int insertSelective(MarketingRole record);

    List<MarketingRole> selectByExample(MarketingRoleExample example);

    MarketingRole selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") MarketingRole record, @Param("example") MarketingRoleExample example);

    int updateByExample(@Param("record") MarketingRole record, @Param("example") MarketingRoleExample example);

    int updateByPrimaryKeySelective(MarketingRole record);

    int updateByPrimaryKey(MarketingRole record);
}