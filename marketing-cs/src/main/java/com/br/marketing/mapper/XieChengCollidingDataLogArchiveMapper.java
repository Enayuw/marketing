package com.br.marketing.mapper;

import com.br.marketing.entity.XieChengCollidingDataLogArchive;

import java.util.List;

public interface XieChengCollidingDataLogArchiveMapper extends XieChengCollidingDataLogArchiveMapperBase{
    void saveBatch(List<XieChengCollidingDataLogArchive> archiveList);
}