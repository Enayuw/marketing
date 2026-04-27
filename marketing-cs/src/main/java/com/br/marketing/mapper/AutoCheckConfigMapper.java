package com.br.marketing.mapper;

import com.br.marketing.entity.AutoCheckConfig;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AutoCheckConfigMapper extends AutoCheckConfigMapperBase{

    /**
     * 根据API编码和场景编码查询配置信息
     * @param apiCodeList API编码列表
     * @param sceneCodeList 场景编码列表
     * @return 配置列表
     */
    List<AutoCheckConfig> selectByApiCodesAndSceneCodes(@Param("apiCodeList") List<String> apiCodeList,
                                                        @Param("sceneCodeList") List<String> sceneCodeList);

    AutoCheckConfig selectByApiCode(@Param("apiCode") String apiCode);

    /**
     * 批量插入配置
     */
    int batchInsert(@Param("list") List<AutoCheckConfig> list);

    /**
     * 批量删除配置（更新 is_deleted / update_time）。
     */
    int batchDelete(@Param("list") List<AutoCheckConfig> list);
}