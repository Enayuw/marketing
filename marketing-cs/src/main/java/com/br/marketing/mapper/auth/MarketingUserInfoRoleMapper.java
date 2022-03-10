package com.br.marketing.mapper.auth;

import com.br.marketing.entity.auth.MarketingUserInfoRole;
import com.br.marketing.entity.auth.MarketingUserInfoRoleExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingUserInfoRoleMapper {
    int countByExample(MarketingUserInfoRoleExample example);

    int deleteByExample(MarketingUserInfoRoleExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(MarketingUserInfoRole record);

    int insertSelective(MarketingUserInfoRole record);

    List<MarketingUserInfoRole> selectByExample(MarketingUserInfoRoleExample example);

    MarketingUserInfoRole selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") MarketingUserInfoRole record, @Param("example") MarketingUserInfoRoleExample example);

    int updateByExample(@Param("record") MarketingUserInfoRole record, @Param("example") MarketingUserInfoRoleExample example);

    int updateByPrimaryKeySelective(MarketingUserInfoRole record);

    int updateByPrimaryKey(MarketingUserInfoRole record);
}