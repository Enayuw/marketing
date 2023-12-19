package com.br.marketing.mapper;

import com.br.marketing.entity.TongChengUndoData;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TongChengUndoDataMapper extends TongChengUndoDataBaseMapper{
    List<TongChengUndoData> tongChengUndoDataPage(@Param("localId") Long localId, @Param("minId") Long minId);
}
