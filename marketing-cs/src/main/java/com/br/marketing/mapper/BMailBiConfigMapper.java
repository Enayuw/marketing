package com.br.marketing.mapper;

import com.br.marketing.entity.BMailBiConfig;
import com.br.marketing.entity.BMailBiConfigExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface BMailBiConfigMapper {
    int countByExample(BMailBiConfigExample example);

    int deleteByExample(BMailBiConfigExample example);

    int deleteByPrimaryKey(Long id);

    int insert(BMailBiConfig record);

    int insertSelective(BMailBiConfig record);

    List<BMailBiConfig> selectByExampleWithBLOBs(BMailBiConfigExample example);

    List<BMailBiConfig> selectByExample(BMailBiConfigExample example);

    BMailBiConfig selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") BMailBiConfig record, @Param("example") BMailBiConfigExample example);

    int updateByExampleWithBLOBs(@Param("record") BMailBiConfig record, @Param("example") BMailBiConfigExample example);

    int updateByExample(@Param("record") BMailBiConfig record, @Param("example") BMailBiConfigExample example);

    int updateByPrimaryKeySelective(BMailBiConfig record);

    int updateByPrimaryKeyWithBLOBs(BMailBiConfig record);

    int updateByPrimaryKey(BMailBiConfig record);
}