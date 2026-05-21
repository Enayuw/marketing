package com.br.marketing.mapper;

import com.br.marketing.entity.TcyrCpaBlankData;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TcyrCpaBlankDataMapper extends TcyrCpaBlankDataMapperBase {

    /**
     * 批量插入；与表上 {@code user_key} 唯一约束配合，重复键静默忽略（MySQL INSERT IGNORE）。
     */
    int batchInsertIgnore(@Param("list") List<TcyrCpaBlankData> list);
}
