package com.br.marketing.mapper.wuba;

import com.br.marketing.entity.wuba.WuBaAiFetchTask;
import com.br.marketing.entity.wuba.WuBaAiFetchTaskExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface WuBaAiFetchTaskMapperBase {
    int countByExample(WuBaAiFetchTaskExample example);

    int deleteByExample(WuBaAiFetchTaskExample example);

    int deleteByPrimaryKey(Long id);

    int insert(WuBaAiFetchTask record);

    int insertSelective(WuBaAiFetchTask record);

    List<WuBaAiFetchTask> selectByExampleWithBLOBs(WuBaAiFetchTaskExample example);

    List<WuBaAiFetchTask> selectByExample(WuBaAiFetchTaskExample example);

    WuBaAiFetchTask selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") WuBaAiFetchTask record, @Param("example") WuBaAiFetchTaskExample example);

    int updateByExampleWithBLOBs(@Param("record") WuBaAiFetchTask record, @Param("example") WuBaAiFetchTaskExample example);

    int updateByExample(@Param("record") WuBaAiFetchTask record, @Param("example") WuBaAiFetchTaskExample example);

    int updateByPrimaryKeySelective(WuBaAiFetchTask record);

    int updateByPrimaryKeyWithBLOBs(WuBaAiFetchTask record);

    int updateByPrimaryKey(WuBaAiFetchTask record);
}