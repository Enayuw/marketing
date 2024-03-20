package com.br.marketing.mapper;

import java.util.List;
import java.util.Map;

public interface XieChengCollidingDataLoopCycleMapper extends XieChengCollidingDataLoopCycleMapperBase{
    List<Map<String, String>> selectPerMinuteCounts();

    Integer selectTodayCycleCount();
}