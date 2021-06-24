package com.br.marketing.mapper;

import com.br.marketing.entity.CustomerInfoPushBatch;
import com.br.marketing.entity.CustomerInfoPushBatchExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CustomerInfoPushBatchMapperBase {
    int countByExample(CustomerInfoPushBatchExample example);

    int deleteByExample(CustomerInfoPushBatchExample example);

    int deleteByPrimaryKey(Long id);

    int insert(CustomerInfoPushBatch record);

    int insertSelective(CustomerInfoPushBatch record);

    List<CustomerInfoPushBatch> selectByExample(CustomerInfoPushBatchExample example);

    CustomerInfoPushBatch selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") CustomerInfoPushBatch record, @Param("example") CustomerInfoPushBatchExample example);

    int updateByExample(@Param("record") CustomerInfoPushBatch record, @Param("example") CustomerInfoPushBatchExample example);

    int updateByPrimaryKeySelective(CustomerInfoPushBatch record);

    int updateByPrimaryKey(CustomerInfoPushBatch record);
}