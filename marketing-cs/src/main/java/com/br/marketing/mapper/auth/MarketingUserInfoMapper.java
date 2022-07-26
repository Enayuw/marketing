package com.br.marketing.mapper.auth;

import com.br.marketing.entity.auth.MarketingUserInfo;
import com.br.marketing.entity.auth.MarketingUserInfoExample;
import com.br.marketing.mysqlInterceptor.AddDataAuth;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface MarketingUserInfoMapper {
    int countByExample(MarketingUserInfoExample example);

    int deleteByExample(MarketingUserInfoExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(MarketingUserInfo record);

    int insertSelective(MarketingUserInfo record);

    List<MarketingUserInfo> selectByExample(MarketingUserInfoExample example);

    MarketingUserInfo selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") MarketingUserInfo record, @Param("example") MarketingUserInfoExample example);

    int updateByExample(@Param("record") MarketingUserInfo record, @Param("example") MarketingUserInfoExample example);

    int updateByPrimaryKeySelective(MarketingUserInfo record);

    int updateByPrimaryKey(MarketingUserInfo record);

    MarketingUserInfo selectUserInfo(MarketingUserInfoExample marketingUserInfoExample);

    List<MarketingUserInfo> selectByExampleList(Map<String,Object> marketingUserInfoExample);
}