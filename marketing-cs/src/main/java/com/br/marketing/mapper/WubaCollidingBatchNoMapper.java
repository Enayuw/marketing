package com.br.marketing.mapper;

import com.br.marketing.entity.WubaCollidingBatchNo;
import com.br.marketing.entity.WubaCollidingBatchNoExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface WubaCollidingBatchNoMapper {
    int countByExample(WubaCollidingBatchNoExample example);

    int deleteByExample(WubaCollidingBatchNoExample example);

    int deleteByPrimaryKey(Long id);

    int insert(WubaCollidingBatchNo record);

    int insertSelective(WubaCollidingBatchNo record);

    List<WubaCollidingBatchNo> selectByExample(WubaCollidingBatchNoExample example);

    WubaCollidingBatchNo selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") WubaCollidingBatchNo record, @Param("example") WubaCollidingBatchNoExample example);

    int updateByExample(@Param("record") WubaCollidingBatchNo record, @Param("example") WubaCollidingBatchNoExample example);

    int updateByPrimaryKeySelective(WubaCollidingBatchNo record);

    int updateByPrimaryKey(WubaCollidingBatchNo record);
}