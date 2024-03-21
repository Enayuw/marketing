package com.br.marketing.mapper;

import com.br.marketing.entity.XieChengCollidingDataLoopCycleArchive;

import java.util.List;

public interface XieChengCollidingDataLoopCycleArchiveMapper extends XieChengCollidingDataLoopCycleArchiveMapperBase{
    void saveBatch(List<XieChengCollidingDataLoopCycleArchive> archiveList);
}