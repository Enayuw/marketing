package com.br.marketing.mapper.auth;

import com.br.marketing.entity.auth.MarketingResource;
import com.br.marketing.entity.auth.MarketingRole;
import com.br.marketing.entity.auth.MarketingUserInfoRole;
import com.br.marketing.entity.auth.MarketingUserInfoRoleExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

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

    List<MarketingRole> getRolesByUid(Integer userId);

    List<MarketingResource> getResourcesByUid(Integer id);

    Set<Integer>  getRoleIds(Integer id);

    List<MarketingUserInfoRole> selectByRoleId(Integer id);
}