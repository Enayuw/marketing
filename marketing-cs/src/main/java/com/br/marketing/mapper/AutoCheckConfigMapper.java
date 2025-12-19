package com.br.marketing.mapper;

import com.br.marketing.entity.AutoCheckConfig;
import org.apache.ibatis.annotations.Param;

import javax.validation.constraints.NotBlank;
import java.util.List;

public interface AutoCheckConfigMapper extends AutoCheckConfigMapperBase{

    /**
     * 根据API编码和场景编码查询配置信息
     * @param apiCodeList API编码列表
     * @param senceCodeList 场景编码列表
     * @return 配置列表
     */
    List<AutoCheckConfig> selectByApiCodesAndSenceCodes(@Param("apiCodeList") List<String> apiCodeList,
                                                        @Param("senceCodeList") List<String> senceCodeList);

    AutoCheckConfig selectByApiCode(@Param("apiCode") String apiCode);
}