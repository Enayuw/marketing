package com.br.marketing.mapper;

import com.br.marketing.dto.tongcheng.TongChengUndoQueryQuantityDTO;
import com.br.marketing.entity.TongChengUndoData;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface TongChengUndoDataMapper extends TongChengUndoDataBaseMapper{
    List<TongChengUndoData> tongChengUndoDataPage(@Param("localId") Long localId, @Param("minId") Long minId);

    List<Map<String, Object>> queryQuantityGroupByLocalId(TongChengUndoQueryQuantityDTO params);
}
