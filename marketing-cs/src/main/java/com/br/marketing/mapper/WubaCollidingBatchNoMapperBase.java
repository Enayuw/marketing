package com.br.marketing.mapper;

import com.br.marketing.entity.WubaCollidingDataBatchNo;
import com.br.marketing.entity.WubaCollidingDataBatchNoExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface WubaCollidingBatchNoMapperBase {
    int countByExample(WubaCollidingDataBatchNoExample example);

    int deleteByExample(WubaCollidingDataBatchNoExample example);

    int deleteByPrimaryKey(Long id);

    int insert(WubaCollidingDataBatchNo record);

    int insertSelective(WubaCollidingDataBatchNo record);

    List<WubaCollidingDataBatchNo> selectByExample(WubaCollidingDataBatchNoExample example);

    WubaCollidingDataBatchNo selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") WubaCollidingDataBatchNo record, @Param("example") WubaCollidingDataBatchNoExample example);

    int updateByExample(@Param("record") WubaCollidingDataBatchNo record, @Param("example") WubaCollidingDataBatchNoExample example);

    int updateByPrimaryKeySelective(WubaCollidingDataBatchNo record);

    int updateByPrimaryKey(WubaCollidingDataBatchNo record);
}