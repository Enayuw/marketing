package com.br.marketing.mapper;

import com.br.marketing.entity.CustomerInfoPushMain;
import com.br.marketing.entity.CustomerInfoPushMainExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface CustomerInfoPushMainMapperBase {
    long countByExample(CustomerInfoPushMainExample example);

    int deleteByExample(CustomerInfoPushMainExample example);

    int deleteByPrimaryKey(Long id);

    int insert(CustomerInfoPushMain record);

    int insertSelective(CustomerInfoPushMain record);

    List<CustomerInfoPushMain> selectByExample(CustomerInfoPushMainExample example);

    CustomerInfoPushMain selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") CustomerInfoPushMain record, @Param("example") CustomerInfoPushMainExample example);

    int updateByExample(@Param("record") CustomerInfoPushMain record, @Param("example") CustomerInfoPushMainExample example);

    int updateByPrimaryKeySelective(CustomerInfoPushMain record);

    int updateByPrimaryKey(CustomerInfoPushMain record);
}