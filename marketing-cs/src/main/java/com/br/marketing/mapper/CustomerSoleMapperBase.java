package com.br.marketing.mapper;

import com.br.marketing.entity.CustomerSole;
import com.br.marketing.entity.CustomerSoleExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CustomerSoleMapperBase {
    int countByExample(CustomerSoleExample example);

    int deleteByExample(CustomerSoleExample example);

    int deleteByPrimaryKey(Long id);

    int insert(CustomerSole record);

    int insertSelective(CustomerSole record);

    List<CustomerSole> selectByExample(CustomerSoleExample example);

    CustomerSole selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") CustomerSole record, @Param("example") CustomerSoleExample example);

    int updateByExample(@Param("record") CustomerSole record, @Param("example") CustomerSoleExample example);

    int updateByPrimaryKeySelective(CustomerSole record);

    int updateByPrimaryKey(CustomerSole record);
}