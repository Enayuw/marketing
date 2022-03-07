package com.br.marketing.mapper;

import com.br.marketing.entity.CustomerCallingDataStatus;
import com.br.marketing.entity.CustomerCallingDataStatusExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface CustomerCallingDataStatusMapper {
    int countByExample(CustomerCallingDataStatusExample example);

    int deleteByExample(CustomerCallingDataStatusExample example);

    int deleteByPrimaryKey(Long id);

    int insert(CustomerCallingDataStatus record);

    int insertSelective(CustomerCallingDataStatus record);

    List<CustomerCallingDataStatus> selectByExample(CustomerCallingDataStatusExample example);

    CustomerCallingDataStatus selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") CustomerCallingDataStatus record, @Param("example") CustomerCallingDataStatusExample example);

    int updateByExample(@Param("record") CustomerCallingDataStatus record, @Param("example") CustomerCallingDataStatusExample example);

    int updateByPrimaryKeySelective(CustomerCallingDataStatus record);

    int updateByPrimaryKey(CustomerCallingDataStatus record);
}