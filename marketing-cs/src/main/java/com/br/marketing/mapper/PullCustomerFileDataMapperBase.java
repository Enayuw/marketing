package com.br.marketing.mapper;

import com.br.marketing.entity.PullCustomerFileData;
import com.br.marketing.entity.PullCustomerFileDataExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PullCustomerFileDataMapperBase {
    int countByExample(PullCustomerFileDataExample example);

    int deleteByExample(PullCustomerFileDataExample example);

    int deleteByPrimaryKey(Long id);

    int insert(PullCustomerFileData record);

    int insertSelective(PullCustomerFileData record);

    List<PullCustomerFileData> selectByExample(PullCustomerFileDataExample example);

    PullCustomerFileData selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") PullCustomerFileData record, @Param("example") PullCustomerFileDataExample example);

    int updateByExample(@Param("record") PullCustomerFileData record, @Param("example") PullCustomerFileDataExample example);

    int updateByPrimaryKeySelective(PullCustomerFileData record);

    int updateByPrimaryKey(PullCustomerFileData record);
}