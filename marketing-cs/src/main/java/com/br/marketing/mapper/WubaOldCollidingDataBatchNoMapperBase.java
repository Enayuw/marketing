package com.br.marketing.mapper;

import com.br.marketing.entity.WubaOldCollidingDataBatchNo;
import com.br.marketing.entity.WubaOldCollidingDataBatchNoExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface WubaOldCollidingDataBatchNoMapperBase {
    int countByExample(WubaOldCollidingDataBatchNoExample example);

    int deleteByExample(WubaOldCollidingDataBatchNoExample example);

    int deleteByPrimaryKey(Long id);

    int insert(WubaOldCollidingDataBatchNo record);

    int insertSelective(WubaOldCollidingDataBatchNo record);

    List<WubaOldCollidingDataBatchNo> selectByExample(WubaOldCollidingDataBatchNoExample example);

    WubaOldCollidingDataBatchNo selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") WubaOldCollidingDataBatchNo record, @Param("example") WubaOldCollidingDataBatchNoExample example);

    int updateByExample(@Param("record") WubaOldCollidingDataBatchNo record, @Param("example") WubaOldCollidingDataBatchNoExample example);

    int updateByPrimaryKeySelective(WubaOldCollidingDataBatchNo record);

    int updateByPrimaryKey(WubaOldCollidingDataBatchNo record);
}