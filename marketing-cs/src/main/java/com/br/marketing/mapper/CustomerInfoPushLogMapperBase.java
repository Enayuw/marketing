package com.br.marketing.mapper;

import com.br.marketing.entity.CustomerInfoPushLog;
import com.br.marketing.entity.CustomerInfoPushLogExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CustomerInfoPushLogMapperBase {
    int countByExample(CustomerInfoPushLogExample example);

    int deleteByExample(CustomerInfoPushLogExample example);

    int deleteByPrimaryKey(Long id);

    int insert(CustomerInfoPushLog record);

    int insertSelective(CustomerInfoPushLog record);

    List<CustomerInfoPushLog> selectByExample(CustomerInfoPushLogExample example);

    CustomerInfoPushLog selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") CustomerInfoPushLog record, @Param("example") CustomerInfoPushLogExample example);

    int updateByExample(@Param("record") CustomerInfoPushLog record, @Param("example") CustomerInfoPushLogExample example);

    int updateByPrimaryKeySelective(CustomerInfoPushLog record);

    int updateByPrimaryKey(CustomerInfoPushLog record);
}