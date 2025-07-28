package com.br.marketing.mapper;

import com.br.marketing.bo.ZhongAnCollidingDataBO;
import com.br.marketing.entity.ZhongAnCollidingConfig;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ZhongAnCollidingConfigMapper extends ZhongAnCollidingConfigMapperBase {
    List<ZhongAnCollidingConfig> queryZhongAnCollidingConfigByPriority();

    List<ZhongAnCollidingDataBO> queryCollidingDataByConfigSql(@Param("completeSql") String completeSql);
}