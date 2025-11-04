package com.br.marketing.mapper;

import com.br.marketing.entity.CostPriceExRecord;
import com.br.marketing.entity.CostPriceExRecordExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CostPriceExRecordMapperBase {
    int countByExample(CostPriceExRecordExample example);

    int deleteByExample(CostPriceExRecordExample example);

    int deleteByPrimaryKey(Long id);

    int insert(CostPriceExRecord record);

    int insertSelective(CostPriceExRecord record);

    List<CostPriceExRecord> selectByExample(CostPriceExRecordExample example);

    CostPriceExRecord selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") CostPriceExRecord record, @Param("example") CostPriceExRecordExample example);

    int updateByExample(@Param("record") CostPriceExRecord record, @Param("example") CostPriceExRecordExample example);

    int updateByPrimaryKeySelective(CostPriceExRecord record);

    int updateByPrimaryKey(CostPriceExRecord record);
}