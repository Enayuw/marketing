package com.br.marketing.mapper.tag;

import com.br.marketing.dto.tag.TagFieldConfigDTO;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 标签字段配置Mapper
 * @author your.name
 * @date 2024/1/x
 */
public interface TagDataFieldConfigMapper extends TagDataFieldConfigMapperBase {
    /**
     * 根据APICode获取字段配置
     */
    List<TagFieldConfigDTO> selectFieldsByApiCode(@Param("apiCode") String apiCode);
}