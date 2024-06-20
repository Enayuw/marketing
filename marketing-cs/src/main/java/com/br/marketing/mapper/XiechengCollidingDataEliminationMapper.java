package com.br.marketing.mapper;

import com.br.marketing.entity.XiechengCollidingDataElimination;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface XiechengCollidingDataEliminationMapper extends XiechengCollidingDataEliminationMapperBase {
    void batchSave(@Param("eliminations") List<XiechengCollidingDataElimination> eliminations);

    List<String> getExcludeData(@Param("sha256CodeList") List<String> sha256CodeList);
}