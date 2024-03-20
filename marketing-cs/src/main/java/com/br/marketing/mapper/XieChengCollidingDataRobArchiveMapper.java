package com.br.marketing.mapper;

import com.br.marketing.entity.XieChengCollidingDataRobArchive;

import java.util.List;

public interface XieChengCollidingDataRobArchiveMapper extends XieChengCollidingDataRobArchiveMapperBase{
    void saveBatch(List<XieChengCollidingDataRobArchive> archiveList);
}