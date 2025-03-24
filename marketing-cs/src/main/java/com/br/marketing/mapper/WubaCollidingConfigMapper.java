package com.br.marketing.mapper;

import com.br.marketing.entity.WubaCollidingConfig;
import com.br.marketing.entity.WubaCollidingData;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface WubaCollidingConfigMapper extends WubaCollidingConfigMapperBase {
    List<WubaCollidingConfig> queryWuBaCollidingConfigByPriority();
    List<WubaCollidingData> queryCollidingDataByConfigSql(@Param("completeSql") String completeSql);
}