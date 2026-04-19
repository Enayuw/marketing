package com.br.marketing.mapper.ningbo;

import com.br.marketing.entity.ningbo.FileReadConfig;
import org.apache.ibatis.annotations.Param;

public interface FileReadConfigMapper extends FileReadConfigMapperBase {

    /**
     * 根据apiCode获取启用的配置
     * @param apiCode API代码
     * @return 配置对象
     */
    FileReadConfig getActiveConfigByApiCode(@Param("apiCode") String apiCode);
}