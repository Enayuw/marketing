package com.br.marketing.mapper;

import com.br.marketing.entity.TongChengUndoData;
import com.br.marketing.entity.TongChengUndoDataExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TongChengUndoDataBaseMapper {
    Long countByExample(TongChengUndoDataExample example);

    int deleteByExample(TongChengUndoDataExample example);

    int deleteByPrimaryKey(Long id);

    int insert(TongChengUndoData record);

    int insertSelective(TongChengUndoData record);

    List<TongChengUndoData> selectByExample(TongChengUndoDataExample example);

    TongChengUndoData selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") TongChengUndoData record, @Param("example") TongChengUndoDataExample example);

    int updateByExample(@Param("record") TongChengUndoData record, @Param("example") TongChengUndoDataExample example);

    int updateByPrimaryKeySelective(TongChengUndoData record);

    int updateByPrimaryKey(TongChengUndoData record);
}