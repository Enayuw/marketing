package com.br.marketing.mapper;

import com.br.marketing.entity.WubaCollidingDataFront;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface WubaCollidingDataRobMapper extends WubaCollidingDataRobMapperBase{
    void batchSaveData(@Param("robs") List<WubaCollidingDataFront> robs);
}