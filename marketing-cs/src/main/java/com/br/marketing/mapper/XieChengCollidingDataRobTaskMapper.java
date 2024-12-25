package com.br.marketing.mapper;

import com.br.marketing.entity.XieChengCollidingDataRobTask;

import java.util.List;

public interface XieChengCollidingDataRobTaskMapper extends XieChengCollidingDataRobTaskMapperBase {
    List<XieChengCollidingDataRobTask> getTask();

    void updateTaskStatusComplete(Long id);
}
